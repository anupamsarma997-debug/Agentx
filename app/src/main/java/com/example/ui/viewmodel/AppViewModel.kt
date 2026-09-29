package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ContentEntity
import com.example.data.local.entity.OpportunityEntity
import com.example.data.local.security.InMemorySecureTokenStore
import com.example.data.local.security.SecureTokenStore
import com.example.data.local.settings.AppSettings
import com.example.data.local.settings.SettingsDataStore
import com.example.data.model.content.ContentLength
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
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
import com.example.data.remote.scout.OfficialCuratedSourceProvider
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
        providers = listOf(OfficialCuratedSourceProvider())
    )
    val opportunityRepository = OpportunityRepository(opportunityDao, scoutEngine)

    // AI Content Engine
    val geminiClient = GeminiClient()
    val contentCreationEngine = ContentCreationEngine(geminiClient)
    val contentRepository = ContentRepository(contentDao, contentCreationEngine)

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

    // Secure token vault (never leaks tokens into UI state or logs)
    val tokenStore: SecureTokenStore = InMemorySecureTokenStore()
    val metaOAuthConfig = MetaOAuthConfig()
    val metaOAuthClient = MetaOAuthClient(metaOAuthConfig, tokenStore)
    val metaConnectionRepository = MetaConnectionRepository(metaOAuthClient, tokenStore)

    val settings: StateFlow<AppSettings> = settingsRepository.settings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AppSettings()
        )

    val metaConnection: StateFlow<MetaConnectionState> = metaConnectionRepository.connectionState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = metaConnectionRepository.connectionState.value
        )

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

    fun showMessage(message: String) {
        _userMessage.value = message
    }

    fun clearMessage() {
        _userMessage.value = null
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
                    length = length
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

    fun connectMetaAccount() {
        val check = metaOAuthClient.checkConfigurationStatus()
        if (check != null) {
            metaConnectionRepository.setError(
                "Meta App configuration is required. Please register an App ID on developers.facebook.com and configure META_APP_ID."
            )
            showMessage("Meta Developer Configuration Required")
        } else {
            // Build authorization URI when configured
            val uri = metaOAuthClient.buildAuthorizationUri()
            if (uri != null) {
                showMessage("Redirecting to Meta OAuth...")
            }
        }
    }

    fun disconnectMeta() {
        metaConnectionRepository.disconnect()
        showMessage("Meta connection removed.")
    }

    /**
     * Connects verified test/preview credentials for architectural validation.
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
        metaConnectionRepository.saveConnection(page, ig, pageToken = "dev_ref_${pageId}")
        showMessage("Meta accounts linked: $pageName & @$instagramUsername")
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

    fun approveContent(contentId: String, contentType: String) {
        viewModelScope.launch {
            val success = verificationRepository.approveContent(contentId, contentType)
            if (success) {
                showMessage("Content draft approved and marked ready for publisher.")
            } else {
                showMessage("Approval rejected: Content failed safety, consistency, or review rules.")
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
}
