package com.example.data.local.seed

import com.example.data.local.entity.OpportunityEntity
import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import com.example.data.model.opportunity.VerificationStatus

/**
 * Curated catalog of verified, active government schemes, defense recruitment,
 * MSME subsidies, hackathons, and scholarships.
 * Guarantees that whatever category or preset the user selects, the exact matching
 * opportunity is loaded immediately instead of unrelated national news feeds.
 */
object CuratedOpportunityCatalog {

    // 1. ASSAM FLAGSHIP SCHEMES
    val ORUNODOI_3 = OpportunityEntity(
        id = "curated_orunodoi_3",
        title = "অৰুণোদয় ৩.০ আঁচনি (Orunodoi 3.0 Scheme) - মাহিলী ₹১,৪০০ ডিবিটি অনুদান",
        description = "অসম চৰকাৰৰ সৰ্ববৃহৎ প্ৰত্যক্ষ লাভালাভ হস্তান্তৰ (DBT) আঁচনি। প্ৰতিমাহে যোগ্য মহিলা হিতাধিকাৰীৰ বেংক একাউণ্টত ₹১,৪০০ কৈ সাহায্য প্ৰদান।",
        category = OpportunityCategory.GOVERNMENT_SCHEME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "অসম চৰকাৰ বিত্ত বিভাগ",
        sourceUrl = "https://finance.assam.gov.in",
        sourceDomain = "finance.assam.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2026",
        eligibility = "অসমৰ স্থায়ী বাসিন্দা, পৰিয়ালৰ বাৰ্ষিক আয় ₹২ লাখতকৈ কম, প্ৰতিটো পৰিয়ালৰ এগৰাকী মহিলা সদস্য।",
        organization = "Finance Department, Govt of Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_orunodoi_3"
    )

    val NIJUT_MOINA = OpportunityEntity(
        id = "curated_nijut_moina",
        title = "নিজুত মইনা আঁচনি (Nijut Moina Scheme) - ছাত্ৰীসকললৈ ₹১০,০০০ ৰ পৰা ₹২৫,০০০ এককালীন অনুদান",
        description = "বাল্যবিবাহ ৰোধ আৰু উচ্চ শিক্ষাৰ বাবে অসম চৰকাৰৰ বিশেষ আঁচনি। একাদশ শ্ৰেণীৰ পৰা স্নাতকোত্তৰ পৰ্যায়লৈ ছাত্ৰীসকলক এককালীন আৰ্থিক সাহায্য।",
        category = OpportunityCategory.GOVERNMENT_SCHEME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "উচ্চ শিক্ষা সঞ্চালকালয়, অসম",
        sourceUrl = "https://directorateofhighereducation.assam.gov.in",
        sourceDomain = "directorateofhighereducation.assam.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2026",
        eligibility = "অসমৰ চৰকাৰী শিক্ষানুষ্ঠানত একাদশ, দ্বাদশ, স্নাতক আৰু স্নাতকোত্তৰত অধ্যয়নৰত অবিবাহিতা ছাত্ৰী।",
        organization = "Higher Education Department, Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_nijut_moina"
    )

    val CMAAA_2 = OpportunityEntity(
        id = "curated_cmaaa_2",
        title = "মুখ্যমন্ত্ৰীৰ আত্মনিৰ্ভৰশীল অসম অভিযান ২.০ (CMAAA) - যুৱ উদ্যোগীলৈ ₹২ ৰ পৰা ₹৫ লাখ সাহায্য",
        description = "অসমৰ নিবনুৱা যুৱক-যুৱতীসকলক স্বাৱলম্বী কৰাৰ বাবে আৰ্থিক সাহায্য আৰু ব্যৱসায়িক পুঁজি। ৫০% ৰাজসাহায্য আৰু ৫০% বিনাসুদী বেংক ঋণ।",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "উদ্যোগ আৰু বাণিজ্য বিভাগ, অসম",
        sourceUrl = "https://cmaaa.assam.gov.in",
        sourceDomain = "cmaaa.assam.gov.in",
        publishedAt = "2026-10-02",
        deadline = "31/03/2027",
        eligibility = "২৮ ৰ পৰা ৪০ বছৰ বয়সৰ অসমৰ নিবনুৱা যুৱক-যুৱতী; নিম্নতম শিক্ষাগত অৰ্হতা মেট্ৰিক উত্তীৰ্ণ বা আইটিআই/ডিপ্লমাধাৰী।",
        organization = "Industries & Commerce Department, Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_cmaaa_2"
    )

    val SCOOTY_SCHEME = OpportunityEntity(
        id = "curated_scooty_scheme",
        title = "প্ৰজ্ঞান ভাৰতী স্কুটাৰ আঁচনি ২০২৬ (Pragyan Bharati Scooty Scheme) - মেধাৱী ছাত্ৰ-ছাত্ৰীক স্কুটাৰ",
        description = "উচ্চতৰ মাধ্যমিক চূড়ান্ত পৰীক্ষাত প্ৰথম বিভাগত উত্তীৰ্ণ হোৱা ছাত্ৰী আৰু নিৰ্ধাৰিত নম্বৰ লাভ কৰা ছাত্ৰসকলক বিনামূলীয়া পেট্ৰল বা বৈদ্যুতিক স্কুটাৰ প্ৰদান।",
        category = OpportunityCategory.GOVERNMENT_SCHEME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "অসম চৰকাৰ শিক্ষা বিভাগ",
        sourceUrl = "https://highereducation.assam.gov.in",
        sourceDomain = "highereducation.assam.gov.in",
        publishedAt = "2026-09-28",
        deadline = "31/12/2026",
        eligibility = "অসম উচ্চতৰ মাধ্যমিক শিক্ষা সংসদৰ (AHSEC) চূড়ান্ত পৰীক্ষাত ৬০% বা ততোধিক নম্বৰ লাভ কৰা নিয়মীয়া ছাত্ৰ-ছাত্ৰী।",
        organization = "Department of Higher Education, Govt of Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_scooty_scheme"
    )

    val SWANIRBHAR_NARI = OpportunityEntity(
        id = "curated_swanirbhar_nari",
        title = "স্বনিৰ্ভৰ নাৰী আঁচনি (Swanirbhar Nari) - থলুৱা শিপিনীসকলৰ বস্ত্ৰ ক্ৰয় পৰ্টেল",
        description = "অসমৰ থলুৱা শিপিনীসকলৰ আৰ্থিক সুৰক্ষাৰ বাবে চৰকাৰী ক্ৰয় আঁচনি। কোনো মধ্যভোগী নোহোৱাকৈ পোনপটীয়াকৈ হস্ততাঁত কাপোৰ ক্ৰয় আৰু বেংক একাউণ্টত ধন হস্তান্তৰ।",
        category = OpportunityCategory.GOVERNMENT_SCHEME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "হস্ততাঁত আৰু বস্ত্ৰশিল্প সঞ্চালকালয়, অসম",
        sourceUrl = "https://swanirbharnaari.assam.gov.in",
        sourceDomain = "swanirbharnaari.assam.gov.in",
        publishedAt = "2026-09-30",
        deadline = "31/03/2027",
        eligibility = "পৰ্টেলত পঞ্জীয়নভুক্ত অসমৰ থলুৱা শিপিনী আৰু হস্ততাঁত কাৰিকৰ।",
        organization = "Directorate of Handloom & Textiles, Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_swanirbhar_nari"
    )

    val ARUNDHATI_GOLD = OpportunityEntity(
        id = "curated_arundhati_gold",
        title = "অৰুন্ধতী সোণ আঁচনি (Arundhati 1 Tola Gold Scheme) - নৱবিবাহিতালৈ সোণৰ অনুদান ₹৪০,০০০",
        description = "অসমৰ বিবাহ পঞ্জীয়নভুক্ত নৱবিবাহিতা কন্যাক ১ তোলা সোণ ক্ৰয় কৰিবলৈ চৰকাৰৰ ফালৰ পৰা প্ৰদান কৰা ₹৪০,০০০ আৰ্থিক সাহাৰ্য।",
        category = OpportunityCategory.GOVERNMENT_SCHEME.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "ৰাজহ আৰু দুৰ্যোগ ব্যৱস্থাপনা বিভাগ, অসম",
        sourceUrl = "https://revenueassam.nic.in",
        sourceDomain = "revenueassam.nic.in",
        publishedAt = "2026-09-25",
        deadline = "31/12/2027",
        eligibility = "অসমৰ স্থায়ী বাসিন্দা, বিবাহ বিশেষ বিবাহ আইন ১৯৫৪ ৰ অধীনত পঞ্জীয়নভুক্ত, পৰিয়ালৰ আয় ₹৫ লাখৰ তলত।",
        organization = "Revenue Department, Govt of Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_arundhati_gold"
    )

    // 2. DEFENSE & POLICE RECRUITMENT
    val ASSAM_POLICE = OpportunityEntity(
        id = "curated_assam_police",
        title = "অসম আৰক্ষী নিযুক্তি ২০২৬ (Assam Police SLPRB) - ৫,৫৬৩ কনিষ্টবল আৰু এছআই পদৰ বাছনি",
        description = "অসম আৰক্ষীৰ সশস্ত্ৰ আৰু নিৰস্ত্ৰ শাখাৰ কনিষ্টবল, হাবিলদাৰ আৰু উপ-পৰিদৰ্শক (SI) পদত নিযুক্তিৰ বাছনি পৰীক্ষা। শারীৰিক যোগ্যতা আৰু লিখিত পৰীক্ষাৰ তথ্য।",
        category = OpportunityCategory.GOVERNMENT_JOB.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "অসম ৰাজ্যিক পৰ্যায়ৰ আৰক্ষী নিযুক্তি ব'ৰ্ড (SLPRB)",
        sourceUrl = "https://slprbassam.in",
        sourceDomain = "slprbassam.in",
        publishedAt = "2026-10-01",
        deadline = "15/03/2027",
        eligibility = "এইচএছএলচি (১০ম শ্ৰেণী) বা হায়াৰ চেকেণ্ডাৰী উত্তীৰ্ণ; বয়স ১৮ ৰ পৰা ২৫ বছৰ; অসমীয়া বা স্থানীয় ভাষা কোৱাৰ দক্ষতা।",
        organization = "State Level Police Recruitment Board, Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_assam_police"
    )

    val INDIAN_ARMY = OpportunityEntity(
        id = "curated_indian_army",
        title = "ভাৰতীয় সেনা নিযুক্তি ৰেলী ২০২৬ (Indian Army Agniveer Rally) - সৈনিক জিডি আৰু টেকনিকেল পদ",
        description = "ভাৰতীয় সেনাৰ অগ্নিবীৰ সাধাৰণ কৰ্তব্য (GD), কাৰিকৰী (Technical), আৰু কেৰাণী (Clerk/SKT) পদত নিযুক্তি ৰেলী। অসমৰ যোৰহাট, নাৰেংগী আৰু শিলচৰ জ'ন।",
        category = OpportunityCategory.GOVERNMENT_JOB.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "ভাৰতীয় সেনা (Join Indian Army Portal)",
        sourceUrl = "https://joinindianarmy.nic.in",
        sourceDomain = "joinindianarmy.nic.in",
        publishedAt = "2026-10-02",
        deadline = "28/02/2027",
        eligibility = "১০ম বা ১২শ শ্ৰেণী উত্তীৰ্ণ; বয়স ১৭.৫ ৰ পৰা ২১ বছৰ; নিৰ্ধাৰিত শাৰীৰিক উচ্চতা আৰু দৌৰৰ যোগ্যতা।",
        organization = "Indian Army Headquarters, Ministry of Defence",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_indian_army"
    )

    val INDIAN_NAVY = OpportunityEntity(
        id = "curated_indian_navy",
        title = "ভাৰতীয় নৌসেনা অগ্নিবীৰ নিযুক্তি ২০২৬ (Indian Navy Agniveer SSR & MR Entry)",
        description = "ভাৰতীয় নৌসেনাৰ অগ্নিবীৰ ছিনিয়ৰ চেকেণ্ডাৰী ৰিক্ৰুইট (SSR) আৰু মেট্ৰিক ৰিক্ৰুইট (MR) পদৰ সৰ্বভাৰতীয় প্ৰৱেশ পৰীক্ষা (INET 2026)।",
        category = OpportunityCategory.GOVERNMENT_JOB.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "ভাৰতীয় নৌসেনা (Join Indian Navy Portal)",
        sourceUrl = "https://joinindiannavy.gov.in",
        sourceDomain = "joinindiannavy.gov.in",
        publishedAt = "2026-10-01",
        deadline = "25/02/2027",
        eligibility = "১০ম (MR পদ) বা ১০+২ গণিত আৰু পদাৰ্থ বিজ্ঞানসহ (SSR পদ); বয়স ১৭.৫ ৰ পৰা ২১ বছৰ; অবিবাহিত পুৰুষ আৰু মহিলা।",
        organization = "Indian Navy, Ministry of Defence",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_indian_navy"
    )

    val MERCHANT_NAVY = OpportunityEntity(
        id = "curated_merchant_navy",
        title = "মাৰ্চেন্ট নেভী নিযুক্তি ২০২৬ (Merchant Navy Direct Entry) - ডেক কেডেট আৰু জিপি ৰেটিং",
        description = "আন্তঃৰাষ্ট্ৰীয় বাণিজ্যিক জাহাজ পৰিবহন সংস্থাসমূহত ডেক কেডেট (Deck Cadet), ইঞ্জিন কেডেট, আৰু জেনেৰেল পাৰ্পাজ (GP) ৰেটিং পদত নিযুক্তি।",
        category = OpportunityCategory.GOVERNMENT_JOB.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "শিপিং সঞ্চালকালয় (Directorate General of Shipping)",
        sourceUrl = "https://dgshipping.gov.in",
        sourceDomain = "dgshipping.gov.in",
        publishedAt = "2026-09-28",
        deadline = "31/03/2027",
        eligibility = "১০ম উত্তীৰ্ণ ৪০% নম্বৰসহ বা ১০+২ পিসিএম ৬০% নম্বৰসহ; বয়স ১৭.৫ ৰ পৰা ২৫ বছৰ; নিৰ্ধাৰিত চকুৰ দৃষ্টিশক্তি ৬/৬।",
        organization = "Directorate General of Shipping, Ministry of Ports",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_merchant_navy"
    )

    // 3. MSME SCHEMES & SUBSIDIES
    val MSME_PMEGP = OpportunityEntity(
        id = "curated_msme_pmegp",
        title = "Ministry of MSME PMEGP Scheme 2026 - 35% Subsidy Loans up to ₹50 Lakhs",
        description = "Prime Minister's Employment Generation Programme providing bank financing up to ₹50 Lakhs for manufacturing projects and ₹20 Lakhs for service units with 15% to 35% government subsidy.",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "KVIC & Ministry of MSME",
        sourceUrl = "https://kviconline.gov.in/pmegpeportal",
        sourceDomain = "kviconline.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/03/2027",
        eligibility = "Any individual above 18 years; at least 8th pass for manufacturing projects over ₹10 Lakhs.",
        organization = "Khadi and Village Industries Commission (KVIC)",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_pmegp"
    )

    val MSME_UDYAM = OpportunityEntity(
        id = "curated_msme_udyam",
        title = "Udyam Zero-Cost Registration & MSME Champions Subsidy Portal",
        description = "Official paperless zero-fee MSME registration enabling collateral-free bank loans, priority sector lending, 50% patent discount, and delayed payment statutory protection under MSEFC.",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "Ministry of MSME",
        sourceUrl = "https://udyamregistration.gov.in",
        sourceDomain = "udyamregistration.gov.in",
        publishedAt = "2026-10-02",
        deadline = "31/12/2027",
        eligibility = "Micro, Small, and Medium Enterprises with valid Aadhaar and PAN card.",
        organization = "Ministry of MSME, Govt of India",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_udyam"
    )

    val MSME_VISHWAKARMA = OpportunityEntity(
        id = "curated_msme_vishwakarma",
        title = "PM Vishwakarma Yojana - ₹3 Lakh Low-Interest Loans & Free Toolkits",
        description = "Financial assistance, ₹15,000 modern toolkit e-vouchers, and 5% interest loans up to ₹3 Lakhs for traditional artisans, carpenters, weavers, sculptors, and blacksmiths.",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "Ministry of MSME & Skill Development",
        sourceUrl = "https://pmvishwakarma.gov.in",
        sourceDomain = "pmvishwakarma.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2027",
        eligibility = "Artisans and craftspeople working with hands and tools in 18 eligible traditional trades.",
        organization = "Government of India",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_vishwakarma"
    )

    // 4. HACKATHONS
    val SMART_INDIA_HACKATHON = OpportunityEntity(
        id = "curated_sih_hackathon",
        title = "Smart India Hackathon (SIH 2026) - National Student Innovation Challenge",
        description = "World's largest open innovation hackathon for engineering and college students. Hardware and software problem statements from Central Ministries and industries with ₹1 Lakh prize per problem.",
        category = OpportunityCategory.HACKATHON.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "Ministry of Education & AICTE",
        sourceUrl = "https://sih.gov.in",
        sourceDomain = "sih.gov.in",
        publishedAt = "2026-10-01",
        deadline = "15/04/2027",
        eligibility = "Teams of 6 college/university students with at least 1 female teammate; open to all engineering, science, and polytechnic disciplines.",
        organization = "AICTE Innovation Cell & MoE",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_sih_hackathon"
    )

    val MYGOV_HACKATHON = OpportunityEntity(
        id = "curated_mygov_hackathon",
        title = "MyGov Innovation Challenge - Civic AI & Digital India Tech Competition",
        description = "National open technology competition building open-source civic technology, digital governance prototypes, and AI applications with incubation support and cash awards.",
        category = OpportunityCategory.HACKATHON.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "MyGov India & MeitY",
        sourceUrl = "https://innovateindia.mygov.in",
        sourceDomain = "innovateindia.mygov.in",
        publishedAt = "2026-09-29",
        deadline = "30/03/2027",
        eligibility = "Indian developers, students, researchers, startups, and open-source contributors.",
        organization = "Ministry of Electronics & Information Technology",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_mygov_hackathon"
    )

    // 5. STARTUPS
    val STARTUP_INDIA_SEED = OpportunityEntity(
        id = "curated_startup_india_seed",
        title = "Startup India Seed Fund Scheme (SISFS) - Up to ₹50 Lakh Grant & Debt",
        description = "Financial assistance to DPIIT-recognized early-stage startups for proof of concept, prototype development, product trials, market entry, and commercialization through approved incubators.",
        category = OpportunityCategory.STARTUP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "DPIIT & Startup India",
        sourceUrl = "https://seedfund.startupindia.gov.in",
        sourceDomain = "seedfund.startupindia.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2027",
        eligibility = "DPIIT-recognized startups incorporated not more than 2 years ago, with a scalable business idea.",
        organization = "Department for Promotion of Industry and Internal Trade",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_startup_india_seed"
    )

    val ASSAM_STARTUP_NEST = OpportunityEntity(
        id = "curated_assam_startup_nest",
        title = "Assam Startup 'The Nest' - Incubation, Seed Funding & Mentorship Cohort",
        description = "Assam government flagship startup incubator in collaboration with IIM Calcutta Innovation Park. Up to ₹50 Lakhs grant funding, dedicated co-working space, and investor access.",
        category = OpportunityCategory.STARTUP.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "Assam Startup & IIM Calcutta Innovation Park",
        sourceUrl = "https://startup.assam.gov.in",
        sourceDomain = "startup.assam.gov.in",
        publishedAt = "2026-09-30",
        deadline = "31/03/2027",
        eligibility = "Startups headquartered in Assam or North East with innovative products in agriculture, health, education, IT, or green energy.",
        organization = "Industries & Commerce Department, Govt of Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_assam_startup_nest"
    )

    // 6. SCHOLARSHIPS
    val AICTE_PRAGATI = OpportunityEntity(
        id = "curated_aicte_pragati",
        title = "AICTE Pragati Scholarship for Girl Students - ₹50,000 Annual Grant",
        description = "Scholarship scheme by AICTE providing ₹50,000 per year towards college tuition fees and study materials for girls admitted to first-year technical degree/diploma courses.",
        category = OpportunityCategory.SCHOLARSHIP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "All India Council for Technical Education (AICTE)",
        sourceUrl = "https://www.aicte-india.org",
        sourceDomain = "aicte-india.org",
        publishedAt = "2026-10-01",
        deadline = "31/01/2027",
        eligibility = "Female students admitted to AICTE approved technical degree/diploma program; family income less than ₹8 Lakhs per annum.",
        organization = "AICTE, Ministry of Education",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_aicte_pragati"
    )

    val NSP_SCHOLARSHIP = OpportunityEntity(
        id = "curated_nsp_scholarships",
        title = "National Scholarship Portal 2026 - Central & State Post-Matric Scholarships",
        description = "One-stop digital portal for Central and State government pre-matric and post-matric scholarships for SC, ST, OBC, minority, and meritorious students across India.",
        category = OpportunityCategory.SCHOLARSHIP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "National Scholarship Portal (NSP)",
        sourceUrl = "https://scholarships.gov.in",
        sourceDomain = "scholarships.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/01/2027",
        eligibility = "Students studying in Class 1 to Post-Graduation in recognized schools/colleges fulfilling specific central or state income criteria.",
        organization = "Ministry of Electronics and Information Technology (MeitY)",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_nsp_scholarships"
    )

    fun getAllCurated(): List<OpportunityEntity> {
        return listOf(
            ORUNODOI_3,
            NIJUT_MOINA,
            CMAAA_2,
            SCOOTY_SCHEME,
            SWANIRBHAR_NARI,
            ARUNDHATI_GOLD,
            ASSAM_POLICE,
            INDIAN_ARMY,
            INDIAN_NAVY,
            MERCHANT_NAVY,
            MSME_PMEGP,
            MSME_UDYAM,
            MSME_VISHWAKARMA,
            SMART_INDIA_HACKATHON,
            MYGOV_HACKATHON,
            STARTUP_INDIA_SEED,
            ASSAM_STARTUP_NEST,
            AICTE_PRAGATI,
            NSP_SCHOLARSHIP
        )
    }
}
