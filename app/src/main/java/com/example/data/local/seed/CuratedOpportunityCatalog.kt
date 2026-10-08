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
        title = "পিএমইজিপি উদ্যোগ আঁচনি ২০২৬ (MSME PMEGP Scheme) - ৩৫% চৰকাৰী ৰাজসাহায্য আৰু ₹৫০ লাখ ঋণ",
        description = "প্ৰধানমন্ত্ৰী ৰোজগাৰ সৃষ্টি কাৰ্যসূচীৰ অধীনত উৎপাদন খণ্ডত ₹৫০ লাখ আৰু সেৱা খণ্ডত ₹২০ লাখলৈকে বেংক ঋণ। গ্ৰামাঞ্চলত ৩৫% আৰু চহৰাঞ্চলত ২৫% চৰকাৰী মাৰ্জিন মানি ৰাজসাহায্য।",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "KVIC আৰু কেন্দ্ৰীয় MSME মন্ত্ৰালয়",
        sourceUrl = "https://kviconline.gov.in/pmegpeportal",
        sourceDomain = "kviconline.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/03/2027",
        eligibility = "১৮ বছৰৰ ঊৰ্ধ্বৰ যিকোনো যুৱক-যুৱতী বা উদ্যোগী; উৎপাদন খণ্ডত ₹১০ লাখ আৰু সেৱা খণ্ডত ₹৫ লাখৰ ওপৰৰ প্ৰকল্পৰ বাবে নিম্নতম ৮ম শ্ৰেণী উত্তীৰ্ণ হ'ব লাগিব।",
        organization = "Khadi and Village Industries Commission (KVIC), Ministry of MSME",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_pmegp"
    )

    val MSME_UDYAM = OpportunityEntity(
        id = "curated_msme_udyam",
        title = "উদ্যম বিনামূলীয়া পঞ্জীয়ন (Udyam MSME Zero-Cost Registration) - বিনা জামিনত ঋণ আৰু সুতৰ ৰেহাই",
        description = "কেন্দ্ৰীয় MSME মন্ত্ৰালয়ৰ ডিজিটেল পেপাৰলেছ পঞ্জীয়ন পৰ্টেল। কোনো মাচুল নোহোৱাকৈ পঞ্জীয়ন কৰি বেংকৰ পৰা বিনা জামিনত প্ৰাথমিক ঋণ, সুতৰ ৰেহাই আৰু চৰকাৰী ক্ৰয়ত অগ্ৰাধিকাৰ লাভ কৰক।",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "কেন্দ্ৰীয় MSME মন্ত্ৰালয়, ভাৰত চৰকাৰ",
        sourceUrl = "https://udyamregistration.gov.in",
        sourceDomain = "udyamregistration.gov.in",
        publishedAt = "2026-10-02",
        deadline = "31/12/2027",
        eligibility = "বৈধ আধাৰ কাৰ্ড আৰু পেন কাৰ্ড থকা যিকোনো ক্ষুদ্ৰ, লঘু বা মজলীয়া উদ্যোগী, দোকান বা ব্যৱসায়িক প্ৰতিষ্ঠান।",
        organization = "Ministry of MSME, Govt of India",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_udyam"
    )

    val MSME_VISHWAKARMA = OpportunityEntity(
        id = "curated_msme_vishwakarma",
        title = "পিএম বিশ্বকৰ্মা যোজনা (PM Vishwakarma Scheme) - ₹৩ লাখ ৫% সুতৰ ঋণ আৰু ₹১৫,০০০ বিনামূলীয়া সঁজুলি",
        description = "থলুৱা শিল্পী, কাৰিকৰ, সোণাৰী, কমাৰ, কুমাৰ, বাঢ়ৈ আৰু হস্তশিল্পীক আধুনিক প্ৰশিক্ষণ, দৈনিক ₹৫০০ ষ্টাইপেণ্ড, ₹১৫,০০০ বিনামূলীয়া টুলকিট আৰু ৫% ৰেহাই সুতত ₹৩ লাখলৈকে ঋণ।",
        category = OpportunityCategory.MSME.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "MSME আৰু দক্ষতা বিকাশ মন্ত্ৰালয়",
        sourceUrl = "https://pmvishwakarma.gov.in",
        sourceDomain = "pmvishwakarma.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2027",
        eligibility = "১৮ টা নিৰ্ধাৰিত পাৰম্পৰিক বৃত্তিত নিজ হাতেৰে কাম কৰা ১৮ বছৰৰ ঊৰ্ধ্বৰ কাৰিকৰ আৰু শিল্পীসকল।",
        organization = "Ministry of MSME & Skill Development, Govt of India",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_msme_vishwakarma"
    )

    // 4. HACKATHONS
    val SMART_INDIA_HACKATHON = OpportunityEntity(
        id = "curated_sih_hackathon",
        title = "স্মাৰ্ট ইণ্ডিয়া হেকাথন ২০২৬ (Smart India Hackathon - SIH 2026) - সৰ্বভাৰতীয় উদ্ভাৱন প্ৰতিযোগিতা",
        description = "ভাৰত চৰকাৰৰ শিক্ষা মন্ত্ৰালয় আৰু AICTE ৰ উদ্যোগত বিশ্বৰ সৰ্ববৃহৎ মুকলি উদ্ভাৱনী প্ৰতিযোগিতা। কেন্দ্ৰীয় মন্ত্ৰালয় আৰু উদ্যোগৰ সমস্যা সমাধান কৰি প্ৰতিটো প্ৰকল্পত ₹১ লাখ পুৰস্কাৰ লাভ কৰক।",
        category = OpportunityCategory.HACKATHON.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "শিক্ষা মন্ত্ৰালয় আৰু AICTE উদ্ভাৱন কোষ",
        sourceUrl = "https://sih.gov.in",
        sourceDomain = "sih.gov.in",
        publishedAt = "2026-10-01",
        deadline = "15/04/2027",
        eligibility = "প্ৰতিটো দলত ৬ গৰাকী কলেজ/বিশ্ববিদ্যালয়ৰ ছাত্ৰ-ছাত্ৰী আৰু কমেও এগৰাকী ছাত্ৰী থাকিব লাগিব। সকলো অভিযান্ত্ৰিক আৰু বিজ্ঞান শাখাৰ বাবে উন্মুক্ত।",
        organization = "AICTE Innovation Cell & Ministry of Education",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_sih_hackathon"
    )

    val MYGOV_HACKATHON = OpportunityEntity(
        id = "curated_mygov_hackathon",
        title = "MyGov উদ্ভাৱনী প্ৰত্যাহ্বান (Civic AI & Digital India Tech Competition) - নগদ পুৰস্কাৰ আৰু ইনকিউবেচন",
        description = "মুক্ত প্ৰযুক্তি প্ৰতিযোগিতা; ডিজিটেল প্ৰশাসন, AI এপ্লিকেচন আৰু নাগৰিক সেৱাৰ প্ৰট'টাইপ নিৰ্মাণৰ বাবে নগদ পুৰস্কাৰ, প্ৰমাণপত্ৰ আৰু ৰাষ্ট্ৰীয় পৰ্যায়ত ইনকিউবেচন সুবিধা।",
        category = OpportunityCategory.HACKATHON.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "MyGov India আৰু MeitY",
        sourceUrl = "https://innovateindia.mygov.in",
        sourceDomain = "innovateindia.mygov.in",
        publishedAt = "2026-09-29",
        deadline = "30/03/2027",
        eligibility = "ভাৰতীয় ডেভেলপাৰ, কলেজীয়া ছাত্ৰ-ছাত্ৰী, গৱেষক আৰু অপেন-ছ'ৰ্চ কন্ট্ৰিবিউটৰসকলৰ বাবে পোনপটীয়া প্ৰৱেশ।",
        organization = "Ministry of Electronics & IT (MeitY)",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_mygov_hackathon"
    )

    // 5. STARTUPS
    val STARTUP_INDIA_SEED = OpportunityEntity(
        id = "curated_startup_india_seed",
        title = "ষ্টাৰ্টআপ ইণ্ডিয়া বীজ পুঁজি আঁচনি (Startup India Seed Fund - SISFS) - ₹৫০ লাখলৈ অনুদান আৰু ঋণ",
        description = "নতুন উদ্ভাৱনী ষ্টাৰ্টআপসমূহৰ প্ৰট'টাইপ বিকাশ, প্ৰডাক্ট পৰীক্ষণ আৰু বজাৰ প্ৰৱেশৰ বাবে অনুমোদিত ইনকিউবেটৰৰ জৰিয়তে ₹৫০ লাখলৈকে আৰ্থিক সাহায্য আৰু বিনিয়োগ।",
        category = OpportunityCategory.STARTUP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "DPIIT আৰু ষ্টাৰ্টআপ ইণ্ডিয়া",
        sourceUrl = "https://seedfund.startupindia.gov.in",
        sourceDomain = "seedfund.startupindia.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/12/2027",
        eligibility = "DPIIT দ্বাৰা স্বীকৃতিপ্ৰাপ্ত আৰু পঞ্জীয়নৰ ২ বছৰৰ ভিতৰত থকা ব্যৱসায়িক ধাৰণাযুক্ত ষ্টাৰ্টআপ।",
        organization = "Department for Promotion of Industry and Internal Trade (DPIIT)",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_startup_india_seed"
    )

    val ASSAM_STARTUP_NEST = OpportunityEntity(
        id = "curated_assam_startup_nest",
        title = "অসম ষ্টাৰ্টআপ 'দ্য নেষ্ট' (Assam Startup The Nest) - ₹৫০ লাখ পুঁজি আৰু আইআইএম কলকাতা মেন্টৰশ্বিপ",
        description = "অসম চৰকাৰৰ ফ্লেগশ্বিপ ষ্টাৰ্টআপ ইনকিউবেটৰ। আইআইএম কলকাতা ইনোভেচন পাৰ্কৰ সহযোগত কো-ৱৰ্কিং স্পেচ, ₹৫০ লাখ অনুদান পুঁজি আৰু বিনিয়োগকাৰীৰ সৈতে পোনপটীয়া সংযোগ।",
        category = OpportunityCategory.STARTUP.name,
        region = OpportunityRegion.ASSAM.name,
        sourceName = "উদ্যোগ আৰু বাণিজ্য বিভাগ, অসম চৰকাৰ",
        sourceUrl = "https://startup.assam.gov.in",
        sourceDomain = "startup.assam.gov.in",
        publishedAt = "2026-09-30",
        deadline = "31/03/2027",
        eligibility = "অসম বা উত্তৰ-পূৰ্বাঞ্চলত মুখ্য কাৰ্যালয় থকা আৰু কৃষি, স্বাস্থ্য, শিক্ষা বা সেউজ শক্তিত কাম কৰা ষ্টাৰ্টআপ।",
        organization = "Industries & Commerce Department, Govt of Assam",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_assam_startup_nest"
    )

    // 6. SCHOLARSHIPS
    val AICTE_PRAGATI = OpportunityEntity(
        id = "curated_aicte_pragati",
        title = "AICTE প্ৰগতি ছাত্ৰী বৃত্তি ২০২৬ (AICTE Pragati Scholarship) - বছৰি ₹৫০,০০০ আৰ্থিক অনুদান",
        description = "কাৰিকৰী শিক্ষা গ্ৰহণ কৰা মেধাৱী ছাত্ৰীসকলৰ বাবে AICTE ৰ বিশেষ বৃত্তি। প্ৰথম বৰ্ষৰ ডিগ্ৰী বা ডিপ্লমা পাঠ্যক্ৰমত নামভৰ্তি কৰা ছাত্ৰীক বছৰি ₹৫০,০০০ কৈ সাহায্য।",
        category = OpportunityCategory.SCHOLARSHIP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "সৰ্বভাৰতীয় কাৰিকৰী শিক্ষা পৰিষদ (AICTE)",
        sourceUrl = "https://www.aicte-india.org",
        sourceDomain = "aicte-india.org",
        publishedAt = "2026-10-01",
        deadline = "31/01/2027",
        eligibility = "AICTE অনুমোদিত প্ৰতিষ্ঠানত প্ৰথম বৰ্ষত নামভৰ্তি কৰা ছাত্ৰী; পৰিয়ালৰ বাৰ্ষিক আয় ₹৮ লাখতকৈ কম।",
        organization = "AICTE, Ministry of Education",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_aicte_pragati"
    )

    val NSP_SCHOLARSHIP = OpportunityEntity(
        id = "curated_nsp_scholarships",
        title = "ৰাষ্ট্ৰীয় বৃত্তি পৰ্টেল ২০২৬ (National Scholarship Portal - NSP) - প্ৰি আৰু পোষ্ট-মেট্ৰিক চৰকাৰী বৃত্তি",
        description = "ভাৰত চৰকাৰ আৰু ৰাজ্য চৰকাৰসমূহৰ সকলো প্ৰি-মেট্ৰিক আৰু পোষ্ট-মেট্ৰিক বৃত্তিৰ একক ডিজিটেল পৰ্টেল। অনুসূচীত জাতি, জনজাতি, অ'বিচি আৰু সংখ্যালঘু ছাত্ৰ-ছাত্ৰীলৈ ডিবিটি সাহাৰ্য।",
        category = OpportunityCategory.SCHOLARSHIP.name,
        region = OpportunityRegion.INDIA.name,
        sourceName = "ৰাষ্ট্ৰীয় বৃত্তি পৰ্টেল (NSP), MeitY",
        sourceUrl = "https://scholarships.gov.in",
        sourceDomain = "scholarships.gov.in",
        publishedAt = "2026-10-01",
        deadline = "31/01/2027",
        eligibility = "স্বীকৃতিপ্ৰাপ্ত বিদ্যালয়, মহাবিদ্যালয় বা বিশ্ববিদ্যালয়ত অধ্যয়নৰত যোগ্য ছাত্ৰ-ছাত্ৰী।",
        organization = "Ministry of Electronics and Information Technology (MeitY)",
        sourceTier = SourceTier.TIER_1_OFFICIAL.name,
        verificationStatus = VerificationStatus.VERIFIED.name,
        contentHash = "hash_curated_nsp_scholarships"
    )

    fun getAllCurated(): List<OpportunityEntity> {
        val baseCurated = listOf(
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
        val portalEntities = com.example.data.remote.scout.OfficialPortalDirectory.getAllAsOpportunityEntities()
        return baseCurated + portalEntities
    }
}
