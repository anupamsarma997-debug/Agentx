package com.example.ui.viewmodel

import android.app.Application
import java.util.UUID
import com.example.domain.model.SourceFact
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AppLogEntity
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.local.logging.AppLogger
import com.example.data.local.security.EncryptedSecureTokenStore
import com.example.data.local.security.SecureTokenStore
import com.example.data.local.settings.AppSettings
import com.example.data.local.settings.SettingsDataStore
import com.example.data.remote.auth.AuthUser
import com.example.data.remote.auth.AuthState
import com.example.data.remote.auth.FirebaseAuthManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.data.model.content.PublishTargetPlatform
import com.example.data.model.meta.FacebookPageInfo
import com.example.data.model.meta.InstagramAccountInfo
import com.example.data.model.meta.InstagramAccountType
import com.example.data.model.meta.MetaConnectionState
import com.example.data.model.meta.MetaConnectionStatus
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.VerificationStatus
import com.example.data.remote.ai.GeminiClient
import com.example.data.remote.meta.MetaOAuthConfig
import com.example.data.remote.meta.MetaOAuthClient
import com.example.data.remote.meta.MetaOAuthResult
import com.example.data.remote.meta.OAuthCallbackOutcome
import com.example.data.remote.scout.OfficialCuratedSourceProvider
import com.example.data.remote.scout.MsmeOpportunitySourceProvider
import com.example.data.remote.scout.BharatSarkarNationalSourceProvider
import com.example.data.remote.scout.StateGovernmentSchemeSourceProvider
import com.example.data.remote.scout.CreatorAndYoutuberNewsSourceProvider
import com.example.data.remote.scout.LiveGovernmentRssSourceProvider
import com.example.data.repository.ContentRepository
import com.example.data.repository.MetaConnectionRepository
import com.example.data.repository.OpportunityRepository
import com.example.data.repository.SettingsRepository
import com.example.domain.automation.DailyAutomationManager
import com.example.domain.automation.FreeTierGuard
import com.example.domain.automation.GenerationDecision
import com.example.domain.automation.WindowStatus
import com.example.domain.engine.ContentCreationEngine
import com.example.domain.engine.ContentCreationOutcome
import com.example.domain.engine.OpportunityScoutEngine
import com.example.domain.generator.PostImageGenerator
import com.example.domain.generator.PostImageSize
import com.example.data.local.entity.ReelDraftEntity
import com.example.data.model.reel.ReelDraft
import com.example.data.model.reel.ReelGenerationOutcome
import com.example.data.model.reel.ReelGenerationStatus
import com.example.data.model.reel.ReelLanguage
import com.example.data.model.reel.ReelSafetyStatus
import com.example.data.model.reel.ReelTopic
import com.example.data.model.reel.ReelType
import com.example.data.repository.ReelRepository
import com.example.domain.engine.ReelEngine
import com.example.domain.engine.ReelSafetyEngine
import com.example.domain.engine.ScoutResult
import com.example.data.local.entity.ContentVersionEntity
import com.example.domain.publishing.GraphApiMetaPublisher
import com.example.domain.publishing.MetaPublisher
import com.example.domain.publishing.PublishResult
import com.example.domain.validator.ContentApprovalValidator
import com.example.domain.validator.ContentField
import com.example.domain.validator.RuleFailure
import com.example.domain.validator.ValidationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.local.entity.FinalVerificationRecordEntity
import com.example.data.local.entity.VerificationAuditLogEntity
import com.example.data.model.verification.FinalVerificationResult
import com.example.data.model.verification.FinalVerificationStatus
import com.example.data.model.verification.PublishReadiness
import com.example.data.model.verification.RejectionReason
import com.example.data.repository.VerificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val settingsDataStore = SettingsDataStore(application.applicationContext)
    val settingsRepository = SettingsRepository(settingsDataStore)
    val freeTierGuard = FreeTierGuard()
    val automationManager = DailyAutomationManager()

    // Room Database & Scout Repositories
    private val database = AppDatabase.getInstance(application.applicationContext)
    val opportunityDao = database.opportunityDao()
    val contentDao = database.contentDao()

    val scoutEngine = OpportunityScoutEngine(
        dao = opportunityDao,
        providers = listOf(
            OfficialCuratedSourceProvider(),
            MsmeOpportunitySourceProvider(),
            BharatSarkarNationalSourceProvider(),
            StateGovernmentSchemeSourceProvider(),
            CreatorAndYoutuberNewsSourceProvider(),
            LiveGovernmentRssSourceProvider()
        )
    )
    val opportunityRepository = OpportunityRepository(opportunityDao, scoutEngine)

    // AI Content Engine
    val postImageGenerator = PostImageGenerator(application.applicationContext)
    val geminiClient = GeminiClient()
    val contentCreationEngine = ContentCreationEngine(geminiClient)
    val contentRepository = ContentRepository(contentDao, contentCreationEngine, postImageGenerator)

    // Meme Engine (Phase 6)
    val memeDao = database.memeDao()
    val memeSafetyEngine = com.example.domain.engine.MemeSafetyEngine()
    val memeEngine = com.example.domain.engine.MemeEngine(geminiClient, memeSafetyEngine, memeDao)
    val memeRepository = com.example.data.repository.MemeRepository(memeDao, memeEngine)

    // Reel Engine (Phase 7)
    val reelDao = database.reelDao()
    val reelSafetyEngine = ReelSafetyEngine()
    val reelEngine = ReelEngine(geminiClient, reelSafetyEngine, freeTierGuard, reelDao)
    val reelRepository = ReelRepository(reelDao, reelEngine)

    // Final Verification & Approval Gate (Phase 8)
    val finalVerificationDao = database.finalVerificationDao()
    val contentVersionDao = database.contentVersionDao()
    val verificationAuditLogDao = database.verificationAuditLogDao()
    val verificationRepository = VerificationRepository(
        verificationDao = finalVerificationDao,
        versionDao = contentVersionDao,
        auditLogDao = verificationAuditLogDao,
        opportunityDao = opportunityDao,
        contentDao = contentDao,
        memeDao = memeDao,
        reelDao = reelDao
    )

    // Secure token vault (hardware-backed AES-256-GCM via Android Keystore, persistent across restarts)
    val tokenStore: SecureTokenStore = EncryptedSecureTokenStore(application.applicationContext)
    val metaOAuthConfig = MetaOAuthConfig()
    val metaOAuthClient = MetaOAuthClient(metaOAuthConfig, tokenStore, application.applicationContext)
    val metaConnectionRepository = MetaConnectionRepository(metaOAuthClient, tokenStore, application.applicationContext)

    // Transparent Content Approval Validator & Real Meta Publisher
    val contentApprovalValidator = ContentApprovalValidator()
    val metaPublisher: MetaPublisher = GraphApiMetaPublisher(tokenStore, application.applicationContext)

    private val _activeValidationResult = MutableStateFlow<Pair<ContentEntity, ValidationResult>?>(null)
    val activeValidationResult: StateFlow<Pair<ContentEntity, ValidationResult>?> = _activeValidationResult.asStateFlow()

    private val _isPublishing = MutableStateFlow(false)
    val isPublishing: StateFlow<Boolean> = _isPublishing.asStateFlow()

    private val _lastPublishResult = MutableStateFlow<String?>(null)
    val lastPublishResult: StateFlow<String?> = _lastPublishResult.asStateFlow()

    // Firebase Auth & Google Sign-In Manager
    val authManager = FirebaseAuthManager(application.applicationContext)
    val authState: StateFlow<AuthState> = authManager.authState
    val currentUser: StateFlow<AuthUser?> = authManager.currentUser

    // Diagnostic In-App Logs Flow
    val appLogs: StateFlow<List<AppLogEntity>> = database.appLogDao().getRecentLogs(150)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val metaConnection: StateFlow<MetaConnectionState> = metaConnectionRepository.connectionState

    // Content Queue state
    val contentQueue: StateFlow<List<ContentEntity>> = contentRepository.observeContentQueue()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val draftContentCount: StateFlow<Int> = contentRepository.countByStatus(GenerationStatus.DRAFT)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val reviewRequiredCount: StateFlow<Int> = contentRepository.countByStatus(GenerationStatus.REVIEW_REQUIRED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val approvedContentCount: StateFlow<Int> = contentRepository.countByStatus(GenerationStatus.APPROVED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val failedContentCount: StateFlow<Int> = contentRepository.countByStatus(GenerationStatus.FAILED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val isGeneratingContent = MutableStateFlow(false)

    // Meme Engine State Flows (Phase 6)
    val allMemes: StateFlow<List<com.example.data.local.entity.MemeDraftEntity>> = memeRepository.observeAllMemes()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val memeCount: StateFlow<Int> = memeRepository.countTotal()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val memeNeedsReviewCount: StateFlow<Int> = memeRepository.countNeedsReview()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val memeApprovedCount: StateFlow<Int> = memeRepository.countApproved()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val isGeneratingMeme = MutableStateFlow(false)

    // Reel Engine State Flows (Phase 7)
    val allReels: StateFlow<List<ReelDraftEntity>> = reelRepository.allReels
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val reelCount: StateFlow<Int> = reelRepository.reelCount
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val reelNeedsReviewCount: StateFlow<Int> = reelRepository.reelNeedsReviewCount
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val reelApprovedCount: StateFlow<Int> = reelRepository.reelApprovedCount
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val isGeneratingReel = MutableStateFlow(false)

    // Verification Engine State Flows (Phase 8)
    val allVerificationRecords: StateFlow<List<FinalVerificationRecordEntity>> = verificationRepository.observeAllRecords()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = emptyList())

    val pendingVerificationCount: StateFlow<Int> = verificationRepository.countByStatus(FinalVerificationStatus.PENDING)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val verifiedNeedsReviewCount: StateFlow<Int> = verificationRepository.countNeedsReview()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val verifiedApprovedCount: StateFlow<Int> = verificationRepository.countByStatus(FinalVerificationStatus.PASSED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val verifiedBlockedCount: StateFlow<Int> = verificationRepository.countByStatus(FinalVerificationStatus.BLOCKED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val verifiedExpiredCount: StateFlow<Int> = verificationRepository.countByStatus(FinalVerificationStatus.EXPIRED)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    // Opportunities state & filters
    val selectedCategoryFilter = MutableStateFlow<OpportunityCategory?>(null)
    val selectedRegionFilter = MutableStateFlow<OpportunityRegion?>(null)
    val isScanning = MutableStateFlow(false)

    val allOpportunities: StateFlow<List<OpportunityEntity>> = opportunityRepository.getActiveOpportunities()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredOpportunities: StateFlow<List<OpportunityEntity>> = combine(
        allOpportunities,
        selectedCategoryFilter,
        selectedRegionFilter
    ) { list, category, region ->
        list.filter { item ->
            val matchCategory = category == null || item.category == category.name
            val matchRegion = region == null || item.region == region.name
            matchCategory && matchRegion
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val totalCount: StateFlow<Int> = opportunityRepository.countTotal()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val verifiedCount: StateFlow<Int> = opportunityRepository.countVerified()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val needsReviewCount: StateFlow<Int> = opportunityRepository.countNeedsReview()
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = 0)

    val lastScoutResult: StateFlow<ScoutResult?> = opportunityRepository.lastScoutResult

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _metaConfigDialogMessage = MutableStateFlow<String?>(null)
    val metaConfigDialogMessage: StateFlow<String?> = _metaConfigDialogMessage.asStateFlow()

    init {
        // Automatically restore saved Meta connection on startup
        metaConnectionRepository.restoreSavedConnection()
        // Start background automation scheduler
        startAutomationScheduler()
        // Auto-seed initial opportunities on first launch if empty
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (opportunityDao.countOpportunitiesSync() == 0) {
                    val s = settings.value
                    opportunityRepository.runScoutScan(
                        assamEnabled = true,
                        northeastEnabled = true,
                        indiaEnabled = true,
                        internationalEnabled = true,
                        newsEnabled = true
                    )
                }
            } catch (e: Exception) {
                AppLogger.warn("Scout", "InitSeed", "Initial scout seed notice: ${e.localizedMessage}")
            }
        }
    }

    fun showMessage(message: String) {
        _userMessage.value = message
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun dismissMetaConfigDialog() {
        _metaConfigDialogMessage.value = null
    }

    fun showMetaConfigDialog(message: String) {
        _metaConfigDialogMessage.value = message
    }

    fun triggerScoutScan() {
        if (isScanning.value) return
        viewModelScope.launch {
            isScanning.value = true
            try {
                val s = settings.value
                val result = opportunityRepository.runScoutScan(
                    assamEnabled = s.assamPriority,
                    northeastEnabled = s.northeastPriority,
                    indiaEnabled = s.indiaOpportunities,
                    internationalEnabled = s.internationalOpportunities,
                    newsEnabled = s.newsCollection
                )
                showMessage("Scout complete: Discovered ${result.discovered} items (${result.newItems} new, ${result.duplicates} duplicates, ${result.verified} verified).")
            } catch (e: Exception) {
                showMessage("Scout scan failed: ${e.localizedMessage ?: "Unknown error"}")
            } finally {
                isScanning.value = false
            }
        }
    }

    fun updateScoutSettings(
        assam: Boolean,
        northeast: Boolean,
        india: Boolean,
        international: Boolean,
        news: Boolean
    ) {
        viewModelScope.launch {
            settingsRepository.updateScoutSettings(assam, northeast, india, international, news)
        }
    }

    fun setCategoryFilter(category: OpportunityCategory?) {
        selectedCategoryFilter.value = category
    }

    fun setRegionFilter(region: OpportunityRegion?) {
        selectedRegionFilter.value = region
    }

    fun updateOpportunityVerification(id: String, status: VerificationStatus) {
        viewModelScope.launch {
            opportunityRepository.updateVerification(id, status)
            showMessage("Verification status updated to ${status.displayName}")
        }
    }

    /**
     * Executes AI generation honoring FreeTierGuard quota and verification gates.
     */
    fun generateContentForOpportunity(
        opportunity: OpportunityEntity,
        contentType: ContentType = ContentType.OPPORTUNITY_POST,
        platform: ContentPlatform = ContentPlatform.BOTH,
        length: ContentLength = ContentLength.SHORT,
        imageSize: PostImageSize = PostImageSize.SQUARE,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val s = settings.value
        val decision = freeTierGuard.canGeneratePost(s)
        if (decision is GenerationDecision.QuotaExhausted) {
            showMessage("Daily free-mode content limit reached: ${s.todayPostCount}/${s.dailyPostTarget} posts generated today. Further generation safely suspended.")
            onComplete?.invoke(false)
            return
        }

        viewModelScope.launch {
            isGeneratingContent.value = true
            try {
                val outcome = contentRepository.generateContent(
                    opportunity = opportunity,
                    contentType = contentType,
                    platform = platform,
                    length = length,
                    imageSize = imageSize
                )

                when (outcome) {
                    is ContentCreationOutcome.Success -> {
                        settingsRepository.incrementTodayPostCount()
                        val statusText = if (outcome.initialStatus == GenerationStatus.REVIEW_REQUIRED) {
                            "Draft created in REVIEW REQUIRED (needs manual approval)."
                        } else {
                            "Draft created in GENERATED state."
                        }
                        showMessage("Content generated successfully! $statusText")
                        onComplete?.invoke(true)
                    }
                    is ContentCreationOutcome.ConfigurationRequired -> {
                        showMessage(outcome.message)
                        onComplete?.invoke(false)
                    }
                    is ContentCreationOutcome.Error -> {
                        showMessage("Generation failed: ${outcome.message}")
                        onComplete?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                showMessage("Generation error: ${e.localizedMessage ?: "Unknown error"}")
                onComplete?.invoke(false)
            } finally {
                isGeneratingContent.value = false
            }
        }
    }

    fun updateContentStatus(id: String, status: GenerationStatus) {
        viewModelScope.launch {
            contentRepository.updateContentStatus(id, status)
            showMessage("Content marked as ${status.displayName}.")
        }
    }

    fun updateContentDraft(
        id: String,
        title: String,
        body: String,
        caption: String,
        hashtags: String
    ) {
        viewModelScope.launch {
            contentRepository.updateContentDraft(id, title, body, caption, hashtags)
            verificationRepository.recordContentEdit(
                contentId = id,
                contentType = "POST",
                changeType = "Content text edit",
                titleSnapshot = title,
                bodySnapshot = body,
                captionSnapshot = caption
            )
            showMessage("Draft saved. Returned to PENDING for re-verification.")
        }
    }

    fun deleteContentDraft(id: String) {
        viewModelScope.launch {
            contentRepository.deleteDraft(id)
            showMessage("Content draft removed from queue.")
        }
    }

    // Meme Operations (Phase 6)
    fun generateMeme(
        topic: com.example.data.model.meme.MemeTopic,
        format: com.example.data.model.meme.MemeFormat = com.example.data.model.meme.MemeFormat.TEXT_MEME,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val s = settings.value
        val decision = freeTierGuard.canGeneratePost(s)
        if (decision is GenerationDecision.QuotaExhausted) {
            showMessage("DAILY_POST_LIMIT_REACHED: Daily free-mode post limit reached (${s.todayPostCount}/${s.dailyPostTarget}). Meme generation suspended.")
            onComplete?.invoke(false)
            return
        }

        viewModelScope.launch {
            isGeneratingMeme.value = true
            try {
                val outcome = memeRepository.generateAndSaveMeme(topic, format)
                when (outcome) {
                    is com.example.domain.engine.MemeGenerationOutcome.Success -> {
                        settingsRepository.incrementTodayPostCount()
                        val statusMsg = if (outcome.draft.safetyStatus == com.example.data.model.meme.MemeSafetyStatus.NEEDS_REVIEW) {
                            "Meme concept generated (Needs editorial review)."
                        } else {
                            "Meme concept generated successfully!"
                        }
                        showMessage(statusMsg)
                        onComplete?.invoke(true)
                    }
                    is com.example.domain.engine.MemeGenerationOutcome.BlockedBySafety -> {
                        showMessage("Meme blocked by safety engine: ${outcome.reasons.joinToString(", ")}")
                        onComplete?.invoke(false)
                    }
                    is com.example.domain.engine.MemeGenerationOutcome.DuplicateDetected -> {
                        showMessage("Duplicate meme rejected: ${outcome.message}")
                        onComplete?.invoke(false)
                    }
                    is com.example.domain.engine.MemeGenerationOutcome.SourceIneligible -> {
                        showMessage("Source ineligible: ${outcome.reason}")
                        onComplete?.invoke(false)
                    }
                    is com.example.domain.engine.MemeGenerationOutcome.ConfigurationRequired -> {
                        showMessage(outcome.message)
                        onComplete?.invoke(false)
                    }
                    is com.example.domain.engine.MemeGenerationOutcome.Error -> {
                        showMessage("Meme generation error: ${outcome.message}")
                        onComplete?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                showMessage("Meme error: ${e.localizedMessage ?: "Unknown error"}")
                onComplete?.invoke(false)
            } finally {
                isGeneratingMeme.value = false
            }
        }
    }

    fun updateMemeStatus(id: String, status: com.example.data.model.meme.MemeGenerationStatus) {
        viewModelScope.launch {
            memeRepository.updateMemeStatus(id, status)
            showMessage("Meme marked as ${status.displayName}.")
        }
    }

    fun updateMemeTexts(
        id: String,
        setupText: String,
        punchlineText: String,
        caption: String,
        hashtags: String
    ) {
        viewModelScope.launch {
            memeRepository.updateMemeTexts(id, setupText, punchlineText, caption, hashtags)
            verificationRepository.recordContentEdit(
                contentId = id,
                contentType = "MEME",
                changeType = "Meme text edit",
                titleSnapshot = setupText,
                bodySnapshot = punchlineText,
                captionSnapshot = caption
            )
            showMessage("Meme draft updated. Returned to PENDING for re-verification.")
        }
    }

    fun deleteMeme(id: String) {
        viewModelScope.launch {
            memeRepository.deleteMeme(id)
            showMessage("Meme removed from queue.")
        }
    }

    fun regenerateMeme(
        draft: com.example.data.local.entity.MemeDraftEntity,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val topic = com.example.data.model.meme.MemeTopic(
            topic = draft.topic,
            context = draft.caption,
            category = "HUMOR",
            region = "ASSAM",
            sourceName = draft.sourceName,
            sourceUrl = draft.sourceUrl,
            verificationStatus = draft.verificationStatus,
            sourceOpportunityId = draft.sourceOpportunityId
        )
        generateMeme(topic, draft.memeFormatEnum, onComplete)
    }

    // ==========================================
    // REEL OPERATIONS (PHASE 7)
    // ==========================================

    fun generateReel(
        topic: ReelTopic,
        reelType: ReelType,
        durationSeconds: Int = 30,
        language: ReelLanguage = ReelLanguage.ENGLISH,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val decision = freeTierGuard.canGenerateReel(settings.value)
        if (decision is GenerationDecision.QuotaExhausted) {
            showMessage(decision.reason)
            onComplete?.invoke(false)
            return
        }
        if (decision is GenerationDecision.TargetReached) {
            showMessage(decision.message)
            onComplete?.invoke(false)
            return
        }

        viewModelScope.launch {
            isGeneratingReel.value = true
            try {
                val outcome = reelRepository.generateReel(
                    topic = topic,
                    reelType = reelType,
                    durationSeconds = durationSeconds,
                    language = language,
                    settings = settings.value
                )
                when (outcome) {
                    is ReelGenerationOutcome.Success -> {
                        // Successful generation consumes 1 reel quota unit
                        settingsRepository.incrementTodayReelCount()
                        val msg = if (outcome.draft.safetyStatus == ReelSafetyStatus.NEEDS_REVIEW) {
                            "Reel plan created (Needs editorial review)."
                        } else {
                            "Reel plan created successfully!"
                        }
                        showMessage(msg)
                        onComplete?.invoke(true)
                    }
                    is ReelGenerationOutcome.BlockedBySafety -> {
                        showMessage("Reel plan blocked by safety engine: ${outcome.reasons.joinToString(", ")}")
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.DuplicateDetected -> {
                        showMessage("Duplicate Reel plan rejected.")
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.QuotaExhausted -> {
                        showMessage("Quota exhausted: ${outcome.reason}")
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.SourceIneligible -> {
                        showMessage("Source ineligible: ${outcome.reason}")
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.ValidationFailed -> {
                        showMessage("Reel validation failed: ${outcome.error}")
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.ConfigurationRequired -> {
                        showMessage(outcome.message)
                        onComplete?.invoke(false)
                    }
                    is ReelGenerationOutcome.Error -> {
                        showMessage("Reel generation error: ${outcome.message}")
                        onComplete?.invoke(false)
                    }
                }
            } catch (e: Exception) {
                showMessage("Reel generation error: ${e.localizedMessage ?: "Unknown error"}")
                onComplete?.invoke(false)
            } finally {
                isGeneratingReel.value = false
            }
        }
    }

    fun updateReelStatus(id: String, status: ReelGenerationStatus) {
        viewModelScope.launch {
            reelRepository.updateStatus(id, status)
            showMessage("Reel marked as ${status.name}.")
        }
    }

    fun updateReelTexts(id: String, hook: String, caption: String, voiceover: String) {
        viewModelScope.launch {
            reelRepository.updateReelTexts(id, hook, caption, voiceover)
            verificationRepository.recordContentEdit(
                contentId = id,
                contentType = "REEL",
                changeType = "Reel text edit",
                titleSnapshot = hook,
                bodySnapshot = voiceover,
                captionSnapshot = caption
            )
            showMessage("Reel plan updated. Returned to PENDING for re-verification.")
        }
    }

    fun deleteReel(id: String) {
        viewModelScope.launch {
            reelRepository.deleteReel(id)
            showMessage("Reel plan removed from queue.")
        }
    }

    fun regenerateReel(
        reel: ReelDraftEntity,
        onComplete: ((Boolean) -> Unit)? = null
    ) {
        val topic = ReelTopic(
            topic = reel.title,
            context = reel.caption,
            sourceName = reel.sourceName,
            sourceUrl = reel.sourceUrl,
            verificationStatus = reel.verificationStatus,
            sourceOpportunityId = reel.sourceOpportunityId,
            sourceContentId = reel.sourceContentId
        )
        generateReel(
            topic = topic,
            reelType = reel.reelTypeEnum,
            durationSeconds = reel.durationSeconds,
            language = reel.languageEnum,
            onComplete = onComplete
        )
    }

    fun connectFacebookPage(onLaunchIntent: (android.net.Uri) -> Unit = {}) {
        val check = metaOAuthClient.checkConfigurationStatus()
        if (check is MetaOAuthResult.ConfigurationRequired) {
            metaConnectionRepository.setError(check.message)
            _metaConfigDialogMessage.value = check.message
            showMessage("Meta Developer Configuration Required")
        } else {
            val uri = metaOAuthClient.buildAuthorizationUri()
            if (uri != null) {
                metaConnectionRepository.setOpeningMeta()
                showMessage("Opening Meta OAuth...")
                try {
                    onLaunchIntent(uri)
                    metaConnectionRepository.setWaitingForAuthorization()
                } catch (e: Exception) {
                    metaConnectionRepository.setError("Failed to launch browser: ${e.localizedMessage}")
                    showMessage("Failed to open browser.")
                }
            } else {
                metaConnectionRepository.setError("Unable to build Meta authorization URL.")
            }
        }
    }

    fun connectMetaAccount() {
        connectFacebookPage()
    }

    fun connectInstagram() {
        val conn = metaConnection.value
        if (!conn.isFacebookConnected) {
            _metaConfigDialogMessage.value = "Connect your Facebook Page first.\n\nMeta requires an eligible Instagram Professional (Business or Creator) account to be associated with an administered Facebook Page to publish content."
            showMessage("Connect your Facebook Page first.")
            return
        }

        val fbPage = conn.facebookPage ?: return
        val pageToken = metaOAuthClient.getPageToken(fbPage.pageId)
            ?: tokenStore.getToken("meta_connected_page_token")
            ?: tokenStore.getToken("meta_page_access_token")

        if (pageToken.isNullOrBlank()) {
            if (conn.isDemoSandbox) {
                showMessage("Demo Facebook Page active: ${fbPage.pageName}")
                return
            }
            _metaConfigDialogMessage.value = "To connect Instagram, please ensure a valid Page Access Token is provided for '${fbPage.pageName}'."
            showMessage("Page Access Token required to link Instagram.")
            return
        }

        viewModelScope.launch {
            metaConnectionRepository.setAuthenticating(true)
            val igResult = metaOAuthClient.fetchInstagramForPage(fbPage.pageId, pageToken)
            metaConnectionRepository.setAuthenticating(false)

            igResult.fold(
                onSuccess = { ig ->
                    if (ig != null) {
                        metaConnectionRepository.saveConnection(
                            facebookPage = fbPage,
                            instagramAccount = ig,
                            pageToken = pageToken,
                            isDemoSandbox = conn.isDemoSandbox
                        )
                        showMessage("Instagram Professional (@${ig.username}) connected!")
                    } else {
                        val notice = "Facebook Page '${fbPage.pageName}' connected hai. Linked Instagram account nahi mila."
                        showMessage(notice)
                        _metaConfigDialogMessage.value = "Facebook Page '${fbPage.pageName}' remains connected!\n\nNo linked Instagram Professional account was detected.\n\nTo link Instagram:\n1. Switch your Instagram account to Professional (Business or Creator) in Instagram app settings.\n2. Link the Instagram account to '${fbPage.pageName}' in Meta Business Suite."
                    }
                },
                onFailure = { err ->
                    val errTxt = err.localizedMessage ?: "Could not verify Instagram"
                    showMessage("Facebook Page connected. Instagram check: $errTxt")
                    _metaConfigDialogMessage.value = "Facebook Page '${fbPage.pageName}' remains connected.\n\nInstagram note: $errTxt"
                }
            )
        }
    }

    fun disconnectFacebook() {
        metaConnectionRepository.disconnectFacebook()
        showMessage("Facebook Page disconnected.")
    }

    fun disconnectInstagram() {
        metaConnectionRepository.disconnectInstagram()
        showMessage("Instagram disconnected.")
    }

    fun reconnectFacebook(onLaunchIntent: (android.net.Uri) -> Unit = {}) {
        disconnectFacebook()
        connectFacebookPage(onLaunchIntent)
    }

    fun reconnectInstagram() {
        disconnectInstagram()
        connectInstagram()
    }

    fun refreshMetaConnection() {
        val conn = metaConnection.value
        if (!conn.isFacebookConnected) {
            metaConnectionRepository.restoreSavedConnection()
            if (metaConnection.value.isFacebookConnected) {
                showMessage("Restored saved Facebook Page connection: ${metaConnection.value.facebookPage?.pageName}")
                return
            }
            showMessage("No accounts connected to refresh.")
            return
        }
        val fb = conn.facebookPage ?: return
        if (conn.isDemoSandbox) {
            showMessage("Demo / Sandbox Facebook connection active: ${fb.pageName}")
            return
        }
        val pageToken = metaOAuthClient.getPageToken(fb.pageId)
            ?: tokenStore.getToken("meta_connected_page_token")
            ?: tokenStore.getToken("meta_page_access_token")

        if (pageToken.isNullOrBlank()) {
            showMessage("Facebook Page connection active: ${fb.pageName}.")
            return
        }

        viewModelScope.launch {
            try {
                val igResult = metaOAuthClient.fetchInstagramForPage(fb.pageId, pageToken)
                igResult.onSuccess { ig ->
                    metaConnectionRepository.saveConnection(
                        facebookPage = fb,
                        instagramAccount = ig ?: conn.instagramAccount,
                        pageToken = pageToken,
                        isDemoSandbox = false
                    )
                    showMessage("Meta connection verified: ${fb.pageName}")
                }.onFailure { _ ->
                    // Do NOT disconnect on transient network failure
                    showMessage("Facebook Page connection active: ${fb.pageName}.")
                }
            } catch (_: Exception) {
                showMessage("Facebook Page connection active: ${fb.pageName}.")
            }
        }
    }

    fun handleOAuthCallback(uri: android.net.Uri) {
        val outcome = metaOAuthClient.parseCallbackUri(uri)
        when (outcome) {
            is OAuthCallbackOutcome.AccessTokenReceived -> {
                val token = outcome.accessToken
                viewModelScope.launch {
                    metaConnectionRepository.setConnecting()
                    val result = metaOAuthClient.connectWithPageAccessToken(token)
                    result.fold(
                        onSuccess = { (fbPage, igAccount) ->
                            metaConnectionRepository.saveConnection(
                                facebookPage = fbPage,
                                instagramAccount = igAccount,
                                pageToken = token,
                                isDemoSandbox = false
                            )
                            val igMsg = if (igAccount != null) " & Instagram: @${igAccount.username}" else ""
                            showMessage("Connected Facebook Page: ${fbPage.pageName}$igMsg")
                        },
                        onFailure = { _ ->
                            val pagesRes = metaOAuthClient.fetchPagesFromGraphApi(token)
                            pagesRes.fold(
                                onSuccess = { pages ->
                                    if (pages.isNotEmpty()) {
                                        val firstPage = pages.first()
                                        val pageToken = metaOAuthClient.getPageToken(firstPage.pageId) ?: token
                                        metaConnectionRepository.saveConnection(
                                            facebookPage = firstPage,
                                            instagramAccount = null,
                                            pageToken = pageToken,
                                            isDemoSandbox = false
                                        )
                                        showMessage("Connected Facebook Page: ${firstPage.pageName}")
                                    } else {
                                        metaConnectionRepository.setError("No administered Facebook Pages found for this account.")
                                    }
                                },
                                onFailure = { fetchErr ->
                                    metaConnectionRepository.setError("Failed to connect Facebook Page: ${fetchErr.message}")
                                }
                            )
                        }
                    )
                }
            }
            is OAuthCallbackOutcome.UserCancelled -> {
                if (!metaConnection.value.isFacebookConnected) {
                    metaConnectionRepository.disconnect()
                }
                showMessage("Meta authorization was cancelled.")
            }
            is OAuthCallbackOutcome.PermissionDenied -> {
                metaConnectionRepository.setPermissionDenied()
                showMessage("Meta authorization was denied by user.")
            }
            is OAuthCallbackOutcome.Error -> {
                metaConnectionRepository.setError(outcome.message)
                showMessage(outcome.message)
            }
            is OAuthCallbackOutcome.CodeReceived -> {
                val code = outcome.code
                val maskedCode = if (code.length > 8) "${code.take(4)}...${code.takeLast(4)}" else "***"
                if (!metaConnection.value.isFacebookConnected) {
                    _metaConfigDialogMessage.value = "Meta Authorization Code Received!\n\n" +
                        "Code: $maskedCode\n\n" +
                        "HOW TO CONNECT RIGHT NOW:\n" +
                        "Please tap 'Connect Facebook Page (Token)' below. You can obtain a Page Access Token directly from Meta Business Suite or the Meta Graph API Explorer (developers.facebook.com/tools/explorer)."
                    showMessage("Meta login successful! Connect with Page Access Token to finalize.")
                } else {
                    showMessage("Meta authorization updated.")
                }
            }
        }
    }

    fun connectWithDirectPageToken(token: String, onComplete: (Boolean) -> Unit = {}) {
        val cleanToken = token.trim()
        if (cleanToken.isBlank()) {
            showMessage("Please enter a valid Page Access Token.")
            return
        }
        viewModelScope.launch {
            metaConnectionRepository.setConnecting()
            val result = metaOAuthClient.connectWithPageAccessToken(cleanToken)
            result.fold(
                onSuccess = { (fbPage, igAccount) ->
                    metaConnectionRepository.saveConnection(
                        facebookPage = fbPage,
                        instagramAccount = igAccount,
                        pageToken = cleanToken,
                        isDemoSandbox = false
                    )
                    val igMsg = if (igAccount != null) " & Instagram: @${igAccount.username}" else " (No Instagram linked)"
                    showMessage("Connected Facebook Page: ${fbPage.pageName}$igMsg")
                    onComplete(true)
                },
                onFailure = { err ->
                    val errMsg = err.localizedMessage ?: "Token validation failed"
                    if (errMsg.contains("190") || errMsg.contains("expired", ignoreCase = true)) {
                        metaConnectionRepository.setExpired()
                    } else {
                        metaConnectionRepository.setError(errMsg)
                    }
                    _metaConfigDialogMessage.value = "Failed to connect Facebook Page:\n\n$errMsg\n\nChecklist:\n1. Verify the token is valid and not expired.\n2. Ensure the token has permissions: pages_show_list, pages_read_engagement, pages_manage_posts.\n3. Make sure the user is an admin of the Facebook Page."
                    showMessage("Connection failed: ${err.message}")
                    onComplete(false)
                }
            )
        }
    }

    fun disconnectMeta() {
        metaConnectionRepository.disconnect()
        showMessage("Meta connection removed.")
    }

    /**
     * Connects verified test/preview credentials for architectural validation.
     * Explicitly marked with isDemoSandbox = true so it is NEVER confused with real Meta connections.
     */
    fun setVerifiedMetaPreview(
        pageName: String,
        pageId: String,
        instagramUsername: String,
        instagramType: InstagramAccountType
    ) {
        val page = FacebookPageInfo(pageId = pageId, pageName = pageName)
        val ig = InstagramAccountInfo(
            instagramAccountId = "ig_$pageId",
            username = instagramUsername,
            accountType = instagramType
        )
        metaConnectionRepository.saveConnection(
            facebookPage = page,
            instagramAccount = ig,
            pageToken = "dev_ref_${pageId}",
            isDemoSandbox = true
        )
        showMessage("DEMO / SANDBOX: $pageName & @$instagramUsername")
    }

    fun setFreeMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setFreeMode(enabled)
        }
    }

    fun setAutomationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutomationEnabled(enabled)
        }
    }

    fun updateAutomationWindow(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) {
        viewModelScope.launch {
            settingsRepository.setAutomationWindow(startHour, startMinute, endHour, endMinute)
        }
    }

    fun updateDailyTargets(postTarget: Int, reelTarget: Int) {
        viewModelScope.launch {
            settingsRepository.setDailyTargets(postTarget, reelTarget)
        }
    }

    fun updateTimezone(timezone: String) {
        viewModelScope.launch {
            settingsRepository.setTimezone(timezone)
        }
    }

    fun resetTodayCounts() {
        viewModelScope.launch {
            settingsRepository.resetTodayCounts()
        }
    }

    fun getWindowStatus(): WindowStatus {
        return automationManager.getWindowStatusDescription(settings.value)
    }

    private val _isAutomationRunning = MutableStateFlow(false)
    val isAutomationRunning: StateFlow<Boolean> = _isAutomationRunning.asStateFlow()

    private var automationJob: Job? = null

    /**
     * Continuous background automation worker.
     * Periodically monitors automation window, auto-restores Meta connection,
     * and triggers automatic generation & publication to Facebook Page without manual intervention.
     */
    fun startAutomationScheduler() {
        if (automationJob?.isActive == true) return
        automationJob = viewModelScope.launch(Dispatchers.Default) {
            delay(3000) // Initial warm-up delay
            while (isActive) {
                try {
                    val s = settings.value
                    if (s.automationEnabled) {
                        // 1. Ensure Meta connection is active from persistent storage
                        if (!metaConnection.value.isFacebookConnected && metaConnectionRepository.hasSavedConnection()) {
                            metaConnectionRepository.restoreSavedConnection()
                        }

                        val isWindowOpen = automationManager.isAutomationWindowOpen(s)
                        val hasRemainingPosts = s.todayPostCount < s.dailyPostTarget
                        val hasUnpublishedPosts = contentDao.getAllContentSync().any {
                            it.facebookPostId.isNullOrBlank() && (it.generationStatus == GenerationStatus.APPROVED.name || it.generationStatus == GenerationStatus.NEEDS_REVIEW.name)
                        }

                        // Run if in window with targets remaining, or if unpublished posts are pending
                        if ((isWindowOpen || hasUnpublishedPosts) && hasRemainingPosts && !_isAutomationRunning.value) {
                            runAutomationJob(isAutoScheduled = true)
                        }
                    }
                } catch (e: Exception) {
                    AppLogger.warn("Automation", "Scheduler", "Background scheduler tick error: ${e.message}")
                }
                delay(30000) // Re-check every 30 seconds
            }
        }
    }

    fun runAutomationJob(isAutoScheduled: Boolean = false) {
        if (_isAutomationRunning.value) return
        viewModelScope.launch {
            _isAutomationRunning.value = true
            val windowDesc = automationManager.getWindowStatusDescription(settings.value)
            AppLogger.info("Automation", "JobStart", "Daily automation job started (Window: ${windowDesc.windowText}, Scheduled: $isAutoScheduled)")
            var postsGenerated = 0
            var postsPublished = 0
            val errors = mutableListOf<String>()

            try {
                // Ensure Facebook Page connection is restored from storage if not already loaded in memory
                if (!metaConnection.value.isFacebookConnected && metaConnectionRepository.hasSavedConnection()) {
                    metaConnectionRepository.restoreSavedConnection()
                }

                // 1. Get verified opportunities from Room. If empty, automatically scan curated sources!
                var verifiedOpportunities = opportunityDao.getOpportunitiesByVerificationStatusSync(VerificationStatus.VERIFIED.name, 10)
                if (verifiedOpportunities.isEmpty()) {
                    AppLogger.info("Automation", "Scout", "No verified opportunities found. Running automatic scout scan...")
                    val s = settings.value
                    try {
                        opportunityRepository.runScoutScan(
                            assamEnabled = s.assamPriority,
                            northeastEnabled = s.northeastPriority,
                            indiaEnabled = s.indiaOpportunities,
                            internationalEnabled = s.internationalOpportunities,
                            newsEnabled = s.newsCollection
                        )
                        verifiedOpportunities = opportunityDao.getOpportunitiesByVerificationStatusSync(VerificationStatus.VERIFIED.name, 10)
                    } catch (e: Exception) {
                        AppLogger.warn("Automation", "Scout", "Auto-scout scan failed: ${e.message}")
                    }
                }

                val existingIds = contentDao.getAllContentSync().map { it.sourceOpportunityId }.toSet()
                val targetOps = verifiedOpportunities.filter { it.id !in existingIds }.take(3)

                for (op in targetOps) {
                    try {
                        val outcome = contentCreationEngine.generateContent(
                            fact = SourceFact.fromEntity(op),
                            contentType = ContentType.OPPORTUNITY_POST,
                            platform = ContentPlatform.BOTH,
                            length = ContentLength.MEDIUM
                        )
                        if (outcome is ContentCreationOutcome.Success) {
                            val newPostId = UUID.randomUUID().toString()
                            val postBanner = postImageGenerator.generatePostBanner(
                                contentId = newPostId,
                                title = outcome.result.title,
                                category = op.category,
                                organization = op.organization ?: outcome.result.sourceName,
                                deadline = op.deadline,
                                sourceUrl = outcome.result.sourceUrl,
                                region = op.region
                            )
                            // In automation mode, auto-approve verified opportunities for immediate Facebook upload
                            contentDao.insertContent(
                                ContentEntity(
                                    id = newPostId,
                                    sourceOpportunityId = op.id,
                                    contentType = ContentType.OPPORTUNITY_POST.name,
                                    platform = ContentPlatform.BOTH.name,
                                    title = outcome.result.title,
                                    body = outcome.result.body,
                                    caption = outcome.result.caption,
                                    hashtags = outcome.result.hashtags.joinToString(", "),
                                    sourceUrl = outcome.result.sourceUrl,
                                    sourceName = outcome.result.sourceName,
                                    generationStatus = GenerationStatus.APPROVED.name,
                                    isSourceVerified = true,
                                    imageUrl = postBanner
                                )
                            )
                            postsGenerated++
                        }
                    } catch (e: Exception) {
                        val err = "Generation failed for '${op.title}': ${e.message}"
                        errors.add(err)
                        AppLogger.error("Automation", "Generate", err)
                    }
                }

                // 2. Publish approved & pending posts to Facebook Page and Instagram
                val pendingPosts = contentDao.getAllContentSync()
                    .filter {
                        it.facebookPostId.isNullOrBlank() &&
                            (it.generationStatus == GenerationStatus.APPROVED.name || it.generationStatus == GenerationStatus.NEEDS_REVIEW.name)
                    }
                    .take(settings.value.dailyPostTarget.coerceAtLeast(3))

                val page = metaConnection.value.facebookPage
                val ig = metaConnection.value.instagramAccount
                val isFb = page != null && metaConnection.value.isFacebookConnected
                val isIg = ig != null && metaConnection.value.isInstagramConnected

                if (isFb || isIg) {
                    for (post in pendingPosts) {
                        val pubMsg = buildString {
                            append(post.body)
                            if (post.hashtags.isNotBlank()) {
                                append("\n\n")
                                append(post.hashtags)
                            }
                        }
                        var fbPostId: String? = null
                        var igPostId: String? = null

                        if (isFb && page != null) {
                            val result = metaPublisher.publishFacebookPost(
                                pageId = page.pageId,
                                content = pubMsg,
                                linkUrl = post.sourceUrl.takeIf { it.startsWith("http://") || it.startsWith("https://") },
                                imageUrl = post.imageUrl
                            )
                            if (result is PublishResult.Success) {
                                fbPostId = result.postId
                            } else if (result is PublishResult.Failure) {
                                errors.add("Facebook failed for '${post.title}': ${result.error}")
                            }
                        }

                        if (isIg && ig != null) {
                            val igResult = metaPublisher.publishInstagramPhoto(
                                instagramAccountId = ig.instagramAccountId,
                                imageUrl = post.imageUrl ?: "",
                                caption = buildString {
                                    append(post.caption.ifBlank { post.title })
                                    if (post.hashtags.isNotBlank()) {
                                        append("\n\n")
                                        append(post.hashtags)
                                    }
                                }
                            )
                            if (igResult is PublishResult.Success) {
                                igPostId = igResult.postId
                            }
                        }

                        if (fbPostId != null || igPostId != null) {
                            val combinedId = listOfNotNull(
                                fbPostId?.let { "fb:$it" },
                                igPostId?.let { "ig:$it" }
                            ).joinToString(" | ")
                            contentDao.updateFacebookPublished(post.id, combinedId)
                            contentDao.updateStatus(post.id, GenerationStatus.PUBLISHED.name)
                            settingsRepository.incrementTodayPostCount()
                            postsPublished++
                            AppLogger.info("Automation", "Publish", "Successfully auto-posted '${post.title}' (ID: $combinedId)")
                        }
                    }
                } else if (pendingPosts.isNotEmpty()) {
                    val notConn = "Meta account connect nahi hai: ${pendingPosts.size} post(s) skipped publication"
                    errors.add(notConn)
                    AppLogger.warn("Automation", "Publish", notConn)
                }

                val summary = buildString {
                    append("Automation Job Finished: Generated $postsGenerated post(s), Published $postsPublished to Meta.")
                    if (errors.isNotEmpty()) {
                        append(" Errors: ${errors.joinToString(" | ")}")
                    }
                }
                if (errors.isEmpty()) {
                    AppLogger.info("Automation", "JobComplete", summary)
                } else {
                    AppLogger.warn("Automation", "JobComplete", summary)
                }
                if (!isAutoScheduled || postsPublished > 0 || errors.isNotEmpty()) {
                    showMessage(summary)
                }
            } catch (e: Exception) {
                val fatal = "Automation job error: ${e.localizedMessage}"
                AppLogger.error("Automation", "JobFatal", fatal, e)
                showMessage(fatal)
            } finally {
                _isAutomationRunning.value = false
            }
        }
    }

    /**
     * Instantly auto-publishes all pending drafts to the connected Facebook Page.
     */
    fun autoPublishPendingPosts() {
        viewModelScope.launch {
            if (!metaConnection.value.isFacebookConnected && metaConnectionRepository.hasSavedConnection()) {
                metaConnectionRepository.restoreSavedConnection()
            }
            val page = metaConnection.value.facebookPage
            if (page == null || !metaConnection.value.isFacebookConnected) {
                showMessage("Facebook Page not connected. Please connect your Page in Settings first.")
                return@launch
            }
            val pendingPosts = contentDao.getAllContentSync()
                .filter { it.facebookPostId.isNullOrBlank() }
            if (pendingPosts.isEmpty()) {
                // If no posts in queue, run automation job to generate and post!
                showMessage("No pending posts found. Starting automation job to generate and post...")
                runAutomationJob()
                return@launch
            }
            showMessage("Auto-uploading ${pendingPosts.size} post(s) to Facebook Page '${page.pageName}'...")
            var count = 0
            for (post in pendingPosts) {
                val pubMsg = buildString {
                    append(post.body)
                    if (post.hashtags.isNotBlank()) {
                        append("\n\n")
                        append(post.hashtags)
                    }
                }
                val res = metaPublisher.publishFacebookPost(
                    pageId = page.pageId,
                    content = pubMsg,
                    linkUrl = post.sourceUrl.takeIf { it.startsWith("http://") || it.startsWith("https://") },
                    imageUrl = post.imageUrl
                )
                if (res is PublishResult.Success) {
                    contentDao.updateFacebookPublished(post.id, res.postId)
                    contentDao.updateStatus(post.id, GenerationStatus.PUBLISHED.name)
                    settingsRepository.incrementTodayPostCount()
                    count++
                }
            }
            showMessage("Successfully auto-uploaded $count post(s) to Facebook Page '${page.pageName}'!")
        }
    }

    // ==========================================
    // FINAL VERIFICATION GATE (PHASE 8)
    // ==========================================

    fun getVerificationRecord(contentId: String): Flow<FinalVerificationRecordEntity?> {
        return verificationRepository.observeRecord(contentId)
    }

    fun getAuditLogs(contentId: String): Flow<List<VerificationAuditLogEntity>> {
        return verificationRepository.observeLogs(contentId)
    }

    fun getContentVersions(contentId: String): Flow<List<ContentVersionEntity>> {
        return verificationRepository.observeVersions(contentId)
    }

    fun runVerification(contentId: String, contentType: String) {
        viewModelScope.launch {
            when (contentType.uppercase()) {
                "POST" -> contentDao.getContentByIdSync(contentId)?.let { verificationRepository.verifyPost(it) }
                "MEME" -> memeDao.getMemeByIdSync(contentId)?.let { verificationRepository.verifyMeme(it) }
                "REEL" -> reelDao.getReelById(contentId)?.let { verificationRepository.verifyReel(it) }
            }
        }
    }

    fun recheckSource(contentId: String, contentType: String) {
        viewModelScope.launch {
            val result = verificationRepository.recheckSource(contentId, contentType)
            if (result != null) {
                showMessage("Source rechecked: Status is ${result.status.displayName}.")
            } else {
                showMessage("Could not recheck source.")
            }
        }
    }

    fun approveContent(
        contentId: String,
        contentType: String,
        overrideWarnings: Boolean = false
    ) {
        viewModelScope.launch {
            if (contentType.equals("POST", ignoreCase = true)) {
                val item = contentDao.getContentByIdSync(contentId)
                if (item == null) {
                    showMessage("Post nahi mila.")
                    return@launch
                }

                // Run transparent independent 8-rule validation
                val validation = contentApprovalValidator.validate(item)

                if (validation.hasBlockingFailures) {
                    val failureSummary = validation.failures.joinToString(separator = "\n") {
                        "Rule ${it.ruleId} (${it.ruleName}): ${it.reason}"
                    }
                    contentDao.updateApprovalValidation(
                        id = item.id,
                        status = GenerationStatus.NEEDS_REVIEW.name,
                        failures = failureSummary,
                        isSourceVerified = false
                    )
                    AppLogger.warn(
                        "Validation",
                        "ApprovalBlocked",
                        "Approval blocked for '${item.title}': ${validation.failures.first().reason}"
                    )
                    _activeValidationResult.value = Pair(item, validation)
                    return@launch
                }

                if (validation.hasOnlyWarnings && !overrideWarnings) {
                    // Warnings exist - require explicit user override via dialog
                    _activeValidationResult.value = Pair(item, validation)
                    return@launch
                }

                // All rules passed or warnings explicitly acknowledged
                contentDao.updateApprovalValidation(
                    id = item.id,
                    status = GenerationStatus.APPROVED.name,
                    failures = null,
                    isSourceVerified = validation.isSourceVerified
                )
                verificationRepository.approveContent(contentId, contentType)
                _activeValidationResult.value = null
                AppLogger.info("Validation", "Approved", "Post '${item.title}' approved successfully.")

                // AUTO-PUBLISH TO FACEBOOK IF CONNECTED!
                val conn = metaConnection.value
                if (conn.isFacebookConnected && conn.facebookPage != null) {
                    showMessage("Post approve ho gaya! Ab Facebook par automatically post ho raha hai...")
                    publishToFacebook(item.id) { success, msg ->
                        if (success) {
                            showMessage("✓ Post approve ho gaya aur Facebook par automatically post ho gaya!")
                        } else {
                            showMessage("Post approve ho gaya, lekin Facebook publish me problem aayi: $msg")
                        }
                    }
                } else {
                    showMessage("Post approved! Facebook connect hone par automatically post kiya ja sakta hai.")
                }
            } else {
                val success = verificationRepository.approveContent(contentId, contentType)
                if (success) {
                    showMessage("$contentType draft approved.")
                } else {
                    showMessage("$contentType approval failed review rules.")
                }
            }
        }
    }

    fun regeneratePostImage(contentId: String, size: PostImageSize = PostImageSize.SQUARE) {
        viewModelScope.launch {
            val item = contentDao.getContentByIdSync(contentId) ?: return@launch
            val newImg = postImageGenerator.generatePostBanner(
                contentId = item.id,
                title = item.title,
                category = item.caption.takeIf { it.isNotBlank() } ?: "Opportunity",
                organization = item.sourceName,
                deadline = null,
                sourceUrl = item.sourceUrl,
                region = null,
                size = size
            )
            if (newImg != null) {
                contentDao.updateImageUrl(item.id, newImg)
                showMessage("${size.displayName} banner image generated successfully!")
            } else {
                showMessage("Failed to generate banner image.")
            }
        }
    }

    fun exportMemeImage(
        memeId: String,
        size: PostImageSize = PostImageSize.SQUARE,
        onResult: (String?) -> Unit = {}
    ) {
        viewModelScope.launch {
            val meme = memeDao.getMemeByIdSync(memeId)
            if (meme == null) {
                showMessage("Meme nahi mila.")
                onResult(null)
                return@launch
            }
            val path = postImageGenerator.generateMemeBanner(
                memeId = meme.id,
                topic = meme.topic,
                setup = meme.setupText,
                punchline = meme.punchlineText,
                formatName = meme.memeFormatEnum.displayName,
                size = size
            )
            if (path != null) {
                showMessage("${size.displayName} meme image created successfully!")
                onResult(path)
            } else {
                showMessage("Failed to generate meme image.")
                onResult(null)
            }
        }
    }

    fun dismissValidationDialog() {
        _activeValidationResult.value = null
    }

    fun publishPostImmediately(
        item: ContentEntity,
        target: PublishTargetPlatform = PublishTargetPlatform.BOTH,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            if (!item.facebookPostId.isNullOrBlank()) {
                val alreadyPublishedMsg = "Ye post pehle se published hai (Post ID: ${item.facebookPostId}). Double posting prevented."
                showMessage(alreadyPublishedMsg)
                AppLogger.warn("Meta", "Publish", alreadyPublishedMsg)
                onComplete(false, alreadyPublishedMsg)
                return@launch
            }

            val page = metaConnection.value.facebookPage
            val ig = metaConnection.value.instagramAccount
            val isFbConnected = metaConnection.value.isFacebookConnected && page != null
            val isIgConnected = metaConnection.value.isInstagramConnected && ig != null

            // Validate connections according to target platform choice
            when (target) {
                PublishTargetPlatform.FACEBOOK_ONLY -> {
                    if (!isFbConnected || page == null) {
                        val msg = "Facebook Page connect nahi hai. Kripya Settings me Facebook Page connect karein."
                        showMessage(msg)
                        AppLogger.warn("Meta", "Publish", msg)
                        onComplete(false, msg)
                        return@launch
                    }
                }
                PublishTargetPlatform.INSTAGRAM_ONLY -> {
                    if (!isIgConnected || ig == null) {
                        val msg = "Instagram account connect nahi hai. Kripya Settings me Instagram Professional connect karein."
                        showMessage(msg)
                        AppLogger.warn("Meta", "Publish", msg)
                        onComplete(false, msg)
                        return@launch
                    }
                }
                PublishTargetPlatform.BOTH -> {
                    if (!isFbConnected && !isIgConnected) {
                        val notConnectedMsg = "Facebook Page ya Instagram account connect nahi hai. Kripya pehle Settings me connect karein."
                        showMessage(notConnectedMsg)
                        AppLogger.warn("Meta", "Publish", notConnectedMsg)
                        onComplete(false, notConnectedMsg)
                        return@launch
                    }
                }
            }

            // Pre-publish URL verification: warn if offline/slow but DO NOT abort publishing
            var verifiedLink: String? = null
            if (item.sourceUrl.isNotBlank() && (item.sourceUrl.startsWith("http://") || item.sourceUrl.startsWith("https://"))) {
                val liveCheck = contentApprovalValidator.validate(item)
                val deadUrl = liveCheck.failures.firstOrNull { it.ruleId == 4 }
                if (deadUrl != null) {
                    AppLogger.warn("Meta", "PublishPreCheck", "Source URL note: ${deadUrl.reason}. Continuing publish without blocking.")
                } else {
                    verifiedLink = item.sourceUrl
                }
            }

            _isPublishing.value = true
            contentDao.updateStatus(item.id, GenerationStatus.PUBLISHING.name)

            val fullMessage = buildString {
                append(item.body)
                if (item.hashtags.isNotBlank()) {
                    append("\n\n")
                    append(item.hashtags)
                }
            }

            var fbPostId: String? = null
            var igPostId: String? = null
            val errors = mutableListOf<String>()

            val shouldPublishFb = (target == PublishTargetPlatform.FACEBOOK_ONLY || target == PublishTargetPlatform.BOTH) && isFbConnected && page != null
            val shouldPublishIg = (target == PublishTargetPlatform.INSTAGRAM_ONLY || target == PublishTargetPlatform.BOTH) && isIgConnected && ig != null

            // 1. Publish to Facebook Page if requested
            if (shouldPublishFb && page != null) {
                AppLogger.info("Meta", "Publish", "Publishing post '${item.title}' to Facebook Page ${page.pageName} (${page.pageId})")
                val result = metaPublisher.publishFacebookPost(
                    pageId = page.pageId,
                    content = fullMessage,
                    linkUrl = verifiedLink ?: item.sourceUrl.takeIf { it.startsWith("http://") || it.startsWith("https://") },
                    imageUrl = item.imageUrl
                )
                when (result) {
                    is PublishResult.Success -> {
                        fbPostId = result.postId
                    }
                    is PublishResult.Failure -> {
                        errors.add("Facebook: ${result.error}")
                    }
                    is PublishResult.Disabled -> {
                        errors.add("Facebook: ${result.message}")
                    }
                }
            }

            // 2. Publish to Instagram if requested
            if (shouldPublishIg && ig != null) {
                AppLogger.info("Meta", "Publish", "Publishing post '${item.title}' to Instagram @${ig.username}")
                val igResult = metaPublisher.publishInstagramPhoto(
                    instagramAccountId = ig.instagramAccountId,
                    imageUrl = item.imageUrl ?: "",
                    caption = buildString {
                        append(item.caption.ifBlank { item.title })
                        if (item.hashtags.isNotBlank()) {
                            append("\n\n")
                            append(item.hashtags)
                        }
                    }
                )
                when (igResult) {
                    is PublishResult.Success -> {
                        igPostId = igResult.postId
                    }
                    is PublishResult.Failure -> {
                        errors.add("Instagram: ${igResult.error}")
                    }
                    is PublishResult.Disabled -> {
                        // ignore disabled
                    }
                }
            }

            if (fbPostId != null || igPostId != null) {
                val combinedId = listOfNotNull(
                    fbPostId?.let { "fb:$it" },
                    igPostId?.let { "ig:$it" }
                ).joinToString(" | ")
                contentDao.updateFacebookPublished(item.id, combinedId)
                val platforms = listOfNotNull(
                    fbPostId?.let { "Facebook Page (${page?.pageName ?: ""})" },
                    igPostId?.let { "Instagram (@${ig?.username ?: ""})" }
                ).joinToString(" & ")
                val successMsg = "Successfully published to $platforms! (ID: $combinedId)"
                AppLogger.info("Meta", "Publish", successMsg)
                showMessage(successMsg)
                _lastPublishResult.value = successMsg
                onComplete(true, successMsg)
            } else {
                contentDao.updateStatus(item.id, GenerationStatus.FAILED.name)
                val failureMsg = "Publishing Failed: ${errors.joinToString(" | ")}"
                AppLogger.error("Meta", "Publish", failureMsg)
                showMessage(failureMsg)
                _lastPublishResult.value = failureMsg
                onComplete(false, failureMsg)
            }
            _isPublishing.value = false
        }
    }

    fun publishWithTarget(
        contentId: String,
        target: PublishTargetPlatform,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val item = contentDao.getContentByIdSync(contentId)
            if (item == null) {
                val msg = "Post nahi mila."
                showMessage(msg)
                onComplete(false, msg)
                return@launch
            }
            publishPostImmediately(item, target, onComplete)
        }
    }

    fun publishToFacebook(
        contentId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        publishWithTarget(contentId, PublishTargetPlatform.FACEBOOK_ONLY, onComplete)
    }

    fun publishToInstagram(
        contentId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        publishWithTarget(contentId, PublishTargetPlatform.INSTAGRAM_ONLY, onComplete)
    }

    fun publishToBoth(
        contentId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        publishWithTarget(contentId, PublishTargetPlatform.BOTH, onComplete)
    }

    fun sendTestPostToFacebookPage(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val page = metaConnection.value.facebookPage
            if (page == null || !metaConnection.value.isFacebookConnected) {
                val msg = "Facebook Page connect nahi hai. Please pehle Settings me Page connect karein."
                showMessage(msg)
                AppLogger.warn("Meta", "TestPost", msg)
                onResult(false, msg)
                return@launch
            }

            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val testContent = "Test post - ignore\n\nSocialAgent Facebook Page integration test.\nTimestamp: $timeStr"

            AppLogger.info("Meta", "TestPost", "Sending test post to Page ${page.pageName} (${page.pageId})")
            val result = metaPublisher.publishFacebookPost(page.pageId, testContent, null)

            when (result) {
                is PublishResult.Success -> {
                    val successMsg = "Test Post published successfully! Post ID: ${result.postId}"
                    AppLogger.info("Meta", "TestPost", successMsg)
                    showMessage(successMsg)
                    onResult(true, successMsg)
                }
                is PublishResult.Failure -> {
                    val errMsg = "Test Post Failed: ${result.error}"
                    AppLogger.error("Meta", "TestPost", errMsg)
                    showMessage(errMsg)
                    onResult(false, errMsg)
                }
                is PublishResult.Disabled -> {
                    val msg = result.message
                    AppLogger.warn("Meta", "TestPost", msg)
                    showMessage(msg)
                    onResult(false, msg)
                }
            }
        }
    }

    fun sendTestPostToInstagram(onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val ig = metaConnection.value.instagramAccount
            if (ig == null || !metaConnection.value.isInstagramConnected) {
                val msg = "Instagram account connect nahi hai. Kripya pehle Settings me Instagram connect karein."
                showMessage(msg)
                AppLogger.warn("Meta", "TestPostIG", msg)
                onResult(false, msg)
                return@launch
            }

            val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
            val testCaption = "SocialAgent Instagram test post.\nTimestamp: $timeStr\n#TestPost #SocialAgent #Instagram"

            AppLogger.info("Meta", "TestPostIG", "Sending test post to Instagram @${ig.username} (${ig.instagramAccountId})")
            val result = metaPublisher.publishInstagramPhoto(
                instagramAccountId = ig.instagramAccountId,
                imageUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?w=800",
                caption = testCaption
            )

            when (result) {
                is PublishResult.Success -> {
                    val successMsg = "Test Post published to Instagram successfully! ID: ${result.postId}"
                    AppLogger.info("Meta", "TestPostIG", successMsg)
                    showMessage(successMsg)
                    onResult(true, successMsg)
                }
                is PublishResult.Failure -> {
                    val errMsg = "Instagram Test Post Failed: ${result.error}"
                    AppLogger.error("Meta", "TestPostIG", errMsg)
                    showMessage(errMsg)
                    onResult(false, errMsg)
                }
                is PublishResult.Disabled -> {
                    val msg = result.message
                    AppLogger.warn("Meta", "TestPostIG", msg)
                    showMessage(msg)
                    onResult(false, msg)
                }
            }
        }
    }

    fun rejectContent(contentId: String, contentType: String, reason: RejectionReason) {
        viewModelScope.launch {
            verificationRepository.rejectContent(contentId, contentType, reason)
            showMessage("Content draft rejected (${reason.displayName}).")
        }
    }

    fun getSourceUrlForContent(contentId: String, contentType: String): String? {
        return when (contentType.uppercase()) {
            "POST" -> contentQueue.value.find { it.id == contentId }?.sourceUrl
            "MEME" -> allMemes.value.find { it.id == contentId }?.sourceUrl
            "REEL" -> allReels.value.find { it.id == contentId }?.sourceUrl
            else -> null
        }
    }

    // Google Sign-In & Authentication
    fun signInWithGoogle(webClientId: String? = null, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val result = authManager.signInWithGoogle(webClientId)
            result.fold(
                onSuccess = { user ->
                    showMessage("Google sign-in successful: ${user.displayName ?: user.email}")
                    onResult(true)
                },
                onFailure = { err ->
                    showMessage(err.message ?: "Google login failed")
                    onResult(false)
                }
            )
        }
    }

    fun signOutGoogle() {
        authManager.signOut()
        showMessage("Signed out successfully.")
    }

    // Diagnostic Logs Management
    fun clearAppLogs() {
        viewModelScope.launch {
            try {
                database.appLogDao().clearAll()
                showMessage("Diagnostic logs cleared.")
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                showMessage("Could not clear logs: ${e.message}")
            }
        }
    }
}
