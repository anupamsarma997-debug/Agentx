package com.example.data.local.seed

import com.example.data.local.dao.ContentDao
import com.example.data.local.entity.ContentEntity
import com.example.data.model.content.ContentPlatform
import com.example.data.model.content.ContentType
import com.example.data.model.content.GenerationStatus
import com.example.data.model.opportunity.VerificationStatus
import com.example.domain.generator.PostImageGenerator
import com.example.domain.generator.PostImageSize
import java.util.UUID

object AssamAndDefenseSeedPosts {

    suspend fun seedPosts(contentDao: ContentDao, postImageGenerator: PostImageGenerator) {
        val existing = contentDao.getAllContentSync()
        val existingMap = existing.associateBy { it.id }

        val seedList = getInitialSeedList()
        for (item in seedList) {
            val current = existingMap[item.id]
            if (current == null) {
                // Generate all 4 sizes (Square, Portrait, Landscape, Story) for each seed post
                var defaultImagePath: String? = null
                for (sz in PostImageSize.entries) {
                    val path = try {
                        postImageGenerator.generatePostBanner(
                            contentId = item.id,
                            title = item.title,
                            category = item.caption,
                            organization = item.sourceName,
                            deadline = "2027",
                            sourceUrl = item.sourceUrl,
                            region = "Assam",
                            size = sz
                        )
                    } catch (e: Exception) {
                        null
                    }
                    if (sz == PostImageSize.SQUARE || defaultImagePath == null) {
                        defaultImagePath = path
                    }
                }

                val finalEntity = item.copy(imageUrl = defaultImagePath)
                contentDao.insertContent(finalEntity)
            } else if (current.imageUrl.isNullOrBlank()) {
                var defaultImagePath: String? = null
                for (sz in PostImageSize.entries) {
                    val path = try {
                        postImageGenerator.generatePostBanner(
                            contentId = current.id,
                            title = current.title,
                            category = current.caption,
                            organization = current.sourceName,
                            deadline = "2027",
                            sourceUrl = current.sourceUrl,
                            region = "Assam",
                            size = sz
                        )
                    } catch (e: Exception) {
                        null
                    }
                    if (sz == PostImageSize.SQUARE || defaultImagePath == null) {
                        defaultImagePath = path
                    }
                }
                if (defaultImagePath != null) {
                    contentDao.updateImageUrl(current.id, defaultImagePath)
                }
            }
        }
    }

    private fun getInitialSeedList(): List<ContentEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            // 1. Assam Police
            ContentEntity(
                id = "post_assam_police_slprb_2026",
                sourceOpportunityId = "slprb_assam_police",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "অসম আৰক্ষী নিযুক্তি ২০২৬ (Assam Police SLPRB 6000+ Posts)",
                body = """
                    📢 অসম আৰক্ষী নিযুক্তি জাননী ২০২৬ (Assam Police Recruitment)
                    
                    অসম আৰক্ষীৰ অধীনত ৬,০০০+ পদৰ বাবে প্ৰাৰ্থী আহ্বান কৰা হৈছে।
                    
                    📌 পদৰ সবিশেষ (Post Details):
                    • উপ-পৰিদৰ্শক (Sub-Inspector - UB)
                    • কনিষ্টবল (Constable - AB & UB)
                    • কমাণ্ডো বেটেলিয়ন জোৱান
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. শিক্ষাগত অৰ্হতা:
                       - কনিষ্টবল পদৰ বাবে: উচ্চতৰ মাধ্যমিক (১২ শ্ৰেণী) উত্তীৰ্ণ
                       - উপ-পৰিদৰ্শক পদৰ বাবে: যিকোনো শাখাত স্নাতক (Graduate)
                    ২. বয়সৰ সীমা: ১৮ ৰ পৰা ২৫ বছৰ (কনিষ্টবল) / ২০ ৰ পৰা ২৬ বছৰ (SI) [সংৰক্ষিত শ্ৰেণীৰ বাবে চৰকাৰী ৰেহাই থাকিব]।
                    ৩. শাৰীৰিক মাপ:
                       - পুৰুষ: ১৬২.৫৬ চেঃমিঃ (Gen/OBC) / ১৬০.০২ চেঃমিঃ (ST)
                       - মহিলা: ১৫৪.৯৪ চেঃমিঃ (Gen/OBC) / ১৫২.৪০ চেঃমিঃ (ST)
                    ৪. শাৰীৰিক সক্ষমতা পৰীক্ষা (PET):
                       - পুৰুষ: ৩২০০ মিটাৰ দৌৰ (১৪ মিনিটত)
                       - মহিলা: ১৬০০ মিটাৰ দৌৰ (৮ মিনিটত)
                    ৫. প্ৰয়োজনীয় নথিপত্ৰ:
                       - অসমৰ স্থায়ী বাসিন্দাৰ প্ৰমাণপত্ৰ (PRC)
                       - এমপ্লয়মেণ্ট এক্সচেঞ্জ পঞ্জীয়ন কাৰ্ড
                       - আধাৰ কাৰ্ড, জাতিৰ প্ৰমাণপত্ৰ, শিক্ষাগত মাৰ্কশ্বীট
                    
                    💰 দৰমহা (Pay Scale):
                    ₹১৪,০০০/- ৰ পৰা ₹৬০,৫০০/- (গ্ৰেড পে' সহিতে)
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    পোনে পোনে ৰাজ্যিক আৰক্ষী নিযুক্তি বোৰ্ডৰ অফিচিয়েল পৰ্টেলত আবেদন কৰক:
                    👉 https://slprbassam.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ৩১ জানুৱাৰী ২০২৭
                """.trimIndent(),
                caption = "অসম আৰক্ষী ৬০০০+ পদৰ নিযুক্তি ২০২৬! কি কি যোগ্যতা লাগিব আৰু ক'ত আবেদন কৰিব চাওক।",
                hashtags = "#AssamPolice, #SLPRB, #AssamGovtJobs, #AssamCareer, #অসম_আৰক্ষী, #JobAlertAssam",
                sourceUrl = "https://slprbassam.in",
                sourceName = "State Level Police Recruitment Board (SLPRB), Assam",
                createdAt = now,
                updatedAt = now,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 2. Indian Army
            ContentEntity(
                id = "post_indian_army_recruitment_2026",
                sourceOpportunityId = "join_indian_army",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "ভাৰতীয় সেনা নিযুক্তি ২০২৬ (Join Indian Army Agniveer Rally & Officers)",
                body = """
                    🇮🇳 ভাৰতীয় সেনা নিযুক্তি ২০২৬ (Indian Army Recruitment Rally)
                    
                    দেশসেৱাৰ গৌৰৱোজ্জ্বল সুযোগ! ভাৰতীয় সেনাত অগ্নিবীৰ আৰু অফিচাৰ এণ্ট্ৰিৰ বাবে জাননী প্ৰকাশ।
                    
                    📌 পদৰ সবিশেষ (Post Details):
                    • অগ্নিবীৰ জেনেৰেল ডিউটি (Agniveer GD)
                    • অগ্নিবীৰ টেকনিকেল (Agniveer Technical)
                    • অগ্নিবীৰ ক্লাৰ্ক / ষ্ট'ৰ কিপাৰ (Clerk / SKT)
                    • টেকনিকেল গ্ৰেজুৱেট কোৰ্ছ (TGC-141 Officer Entry)
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. বয়সৰ সীমা: ১৭.৫ ৰ পৰা ২১ বছৰ (অগ্নিবীৰৰ বাবে) / ২০ ৰ পৰা ২৭ বছৰ (TGC অফিচাৰৰ বাবে)।
                    ২. শিক্ষাগত অৰ্হতা:
                       - অগ্নিবীৰ GD: ১০ম শ্ৰেণী উত্তীৰ্ণ (প্ৰতিটো বিষয়ত ৩৩% সহ মুঠ ৪৫% নম্বৰ)।
                       - টেকনিকেল: ১০+২ বিজ্ঞান শাখা (Physics, Chemistry, Maths & English) ৫০% নম্বৰ সহ।
                       - অফিচাৰ এণ্ট্ৰি: ইঞ্জিনীয়াৰিং স্নাতক ডিগ্ৰী (BE / B.Tech)।
                    ৩. শাৰীৰিক সক্ষমতা পৰীক্ষা (Physical Fitness Test):
                       - ১.৬ কিঃমিঃ দৌৰ (৫ মিনিট ৩০ ছেকেণ্ড - গ্ৰুপ ১ / ৫ মিনিট ৪৫ ছেকেণ্ড - গ্ৰুপ ২)।
                       - বিম (Pull-ups): ১০ টা (৪০ নম্বৰ)।
                       - ৯ ফুট গাত জপন আৰু জিক-জেক বেলেন্স পৰীক্ষা।
                    ৪. প্ৰয়োজনীয় নথিপত্ৰ:
                       - আধাৰ কাৰ্ড আৰু পেন কাৰ্ড।
                       - বিদ্যালয়/মহাবিদ্যালয়ৰ প্ৰৱেশ পত্ৰ আৰু মাৰ্কশ্বীট।
                       - গাঁওবুঢ়া/পৌৰনিগমৰ চৰিত্ৰ আৰু অবিবাহিত প্ৰমাণপত্ৰ।
                    
                    💰 আৰ্থিক সুবিধা (Salary & Package):
                    প্ৰথম বছৰ মাহে ₹৩০,০০০/-, চতুৰ্থ বছৰ ₹৪০,০০০/- + ৪ বছৰৰ পাছত ₹১১.৭১ লাখ সেৱা নিধি পেকেজ (কৰমুক্ত)।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    ভাৰতীয় সেনাৰ অফিচিয়েল ৱেবচাইটত অনলাইন পঞ্জীয়ন কৰক:
                    👉 https://joinindianarmy.nic.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ২৫ ফেব্ৰুৱাৰী ২০২৭
                """.trimIndent(),
                caption = "ভাৰতীয় সেনাত যোগদানৰ সোণালী সুযোগ! কি কি যোগ্যতা লাগিব আৰু ক'ত আবেদন কৰিব চাওক।",
                hashtags = "#JoinIndianArmy, #IndianArmyRally, #Agniveer2026, #ArmyJob, #ভাৰতীয়_সেনা, #DefenceJobs",
                sourceUrl = "https://joinindianarmy.nic.in",
                sourceName = "Indian Army Recruiting Directorate, Ministry of Defence",
                createdAt = now - 1000,
                updatedAt = now - 1000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 3. Indian Navy
            ContentEntity(
                id = "post_indian_navy_recruitment_2026",
                sourceOpportunityId = "join_indian_navy",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "ভাৰতীয় নৌসেনা নিযুক্তি ২০২৬ (Join Indian Navy SSR & MR Recruitment)",
                body = """
                    ⚓ ভাৰতীয় নৌসেনা নিযুক্তি ২০২৬ (Join Indian Navy Recruitment)
                    
                    সাগৰৰ বুকুত দেশৰ সুৰক্ষা আৰু উজ্জ্বল ভৱিষ্যতৰ বাবে ভাৰতীয় নৌসেনাত নিযুক্তি প্ৰক্ৰিয়া আৰম্ভ।
                    
                    📌 পদৰ সবিশেষ (Post Details):
                    • অগ্নিবীৰ এছ.এছ.আৰ (Senior Secondary Recruit - SSR)
                    • অগ্নিবীৰ এম.আৰ (Matric Recruit - MR - Chef, Steward & Hygienist)
                    • এক্সিকিউটিভ আৰু এডুকেশ্যন ব্ৰাঞ্চ অফিচাৰ
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. বয়সৰ সীমা: ১৭.৫ ৰ পৰা ২১ বছৰ।
                    ২. শিক্ষাগত অৰ্হতা:
                       - অগ্নিবীৰ SSR: ১০+২ পৰীক্ষাত গণিত আৰু পদাৰ্থ বিজ্ঞান (Maths & Physics) সহ ন্যূনতম ৫০% নম্বৰ।
                       - অগ্নিবীৰ MR: ১০ম শ্ৰেণী উত্তীৰ্ণ যিকোনো স্বীকৃতিপ্ৰাপ্ত বোৰ্ডৰ পৰা।
                    ৩. শাৰীৰিক সক্ষমতা (Physical Test):
                       - ১.৬ কিঃমিঃ দৌৰ: পুৰুষ - ৬ মিনিট ৩০ ছেকেণ্ড / মহিলা - ৮ মিনিট।
                       - উঠক-বৈঠক (Squats): পুৰুষ - ২০ টা / মহিলা - ১৫ টা।
                       - পুশ-আপ (Push-ups): পুৰুষ - ১২ টা।
                    ৪. চকুৰ দৃষ্টি (Eyesight):
                       - চশমা অবিহনে ৬/৬ (ভাল চকু) আৰু ৬/৯ (বেয়া চকু)। ৰং অন্ধতা (Color blindness) থাকিব নোৱাৰিব।
                    ৫. প্ৰয়োজনীয় নথিপত্ৰ:
                       - শিক্ষাগত প্ৰমাণপত্ৰ আৰু মাৰ্কশ্বীট।
                       - আধাৰ কাৰ্ড আৰু পাছপোৰ্ট ফটো।
                       - স্থায়ী বাসিন্দাৰ প্ৰমাণপত্ৰ।
                    
                    💰 দৰমহা আৰু সা-সুবিধা:
                    মাহেকীয়া দৰমহা ₹৩০,০০০ - ₹৪০,০০০ + ফ্ৰী ৰেচন, থকা-খোৱাৰ সুবিধা, ইউনিফৰ্ম এলাউন্স আৰু সেৱা নিধি পেকেজ।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    ভাৰতীয় নৌসেনাৰ অফিচিয়েল ৱেবচাইটলৈ গৈ অনলাইন আবেদন কৰক:
                    👉 https://joinindiannavy.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ১৫ জানুৱাৰী ২০২৭
                """.trimIndent(),
                caption = "ভাৰতীয় নৌসেনাত অগ্নিবীৰ SSR আৰু MR নিযুক্তি! কেনেকৈ আবেদন কৰিব চাওক।",
                hashtags = "#IndianNavy, #NavyRecruitment, #AgniveerNavy, #DefenceCareers, #ভাৰতীয়_নৌসেনা, #NavyLife",
                sourceUrl = "https://joinindiannavy.gov.in",
                sourceName = "Indian Navy, Ministry of Defence",
                createdAt = now - 2000,
                updatedAt = now - 2000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 4. Merchant Navy
            ContentEntity(
                id = "post_merchant_navy_recruitment_2026",
                sourceOpportunityId = "merchant_navy_india",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "মাৰ্চেন্ট নেভী কেৰিয়াৰ নিযুক্তি ২০২৬ (Merchant Navy Deck Cadet & GP Rating)",
                body = """
                    🚢 মাৰ্চেন্ট নেভী কেৰিয়াৰ নিযুক্তি ২০২৬ (Merchant Navy Opportunities)
                    
                    বিশ্বজুৰি ভ্ৰমণ আৰু বিশাল দৰমহাৰ আকৰ্ষণীয় সুযোগ! বাণিজ্যিক জাহাজত কেৰিয়াৰ গঢ়াৰ সোণালী সময়।
                    
                    📌 পদৰ সবিশেষ (Post Details):
                    • ডেক কেডেট (Deck Cadet - Navigation Officer)
                    • ইঞ্জিন কেডেট (Engine Cadet / Junior Marine Engineer)
                    • জি.পি ৰেটিং (General Purpose Rating - Seaman)
                    • কেটাৰিং ক্ৰু (Saloon / Catering Staff)
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. বয়সৰ সীমা: ১৭.৫ ৰ পৰা ২৫ বছৰ।
                    ২. শিক্ষাগত অৰ্হতা:
                       - ডেক কেডেটৰ বাবে: ১০+২ বিজ্ঞান শাখা (Physics, Chemistry, Maths) ৬০% নম্বৰ সহ আৰু ইংৰাজীত ন্যূনতম ৫০% নম্বৰ।
                       - ইঞ্জিন কেডেটৰ বাবে: মেকানিকেল / মেৰিন ইঞ্জিনীয়াৰিং স্নাতক ডিগ্ৰী।
                       - GP Rating ৰ বাবে: ১০ম শ্ৰেণী উত্তীৰ্ণ (৪০% নম্বৰ সহ)।
                    ৩. শাৰীৰিক আৰু স্বাস্থ্য পৰীক্ষা (Medical Fitness):
                       - ডিজি শিপিং (DG Shipping) অনুমোদিত চিকিৎসকৰ দ্বাৰা সম্পূৰ্ণ ফিটনেছ চাৰ্টিফিকেট বাধ্যতামূলক।
                       - দৃষ্টিশক্তি ৬/৬ (ডেক কেডেটৰ বাবে কোনো চশমা গ্ৰহণযোগ্য নহয়; ৰং অন্ধতা থাকিব নোৱাৰিব)।
                    ৪. প্ৰয়োজনীয় নথিপত্ৰ:
                       - বৈধ ভাৰতীয় পাছপোৰ্ট (Passport - অতি আৱশ্যকীয়)।
                       - আধাৰ কাৰ্ড আৰু শিক্ষাগত মূল প্ৰমাণপত্ৰ।
                       - CDC (Continuous Discharge Certificate - প্ৰশিক্ষণৰ পাছত লাভ কৰিব)।
                    
                    💰 আকৰ্ষণীয় দৰমহা (Tax-Free Salary):
                    প্ৰশিক্ষণ কালত মাহে ₹২৫,০০০ - ₹৫০,০০০ ষ্টাইপেণ্ড। সফল প্ৰশিক্ষণৰ পাছত আৰম্ভণিতেই মাহে ₹৮০,০০০ ৰ পৰা ₹২,৫০,০০০+ পৰ্যন্ত কৰমুক্ত (Tax-free) দৰমহা।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    ডিজি শিপিং অফিচিয়েল প'ৰ্টেল আৰু ইণ্ডিয়ান মেৰিটাইম ইউনিভাৰ্চিটী (IMU) ত আবেদন কৰক:
                    👉 https://dgshipping.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ২০ ফেব্ৰুৱাৰী ২০২৭
                """.trimIndent(),
                caption = "মাৰ্চেন্ট নেভীত কেৰিয়াৰ আৰম্ভ কৰক! বিশাল দৰমহা আৰু বিদেশ ভ্ৰমণৰ সুযোগ।",
                hashtags = "#MerchantNavy, #DeckCadet, #GPRating, #DGShipping, #MarineEngineer, #HighSalaryJobs",
                sourceUrl = "https://dgshipping.gov.in",
                sourceName = "Directorate General of Shipping (DGS), Ministry of Ports & Shipping",
                createdAt = now - 3000,
                updatedAt = now - 3000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 5. Nijut Moina Asoni
            ContentEntity(
                id = "post_nijut_moina_asoni_2026",
                sourceOpportunityId = "mmnma_assam_gov",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "মুখ্যমন্ত্ৰী নিজুত মইনা আঁচনি ২০২৬ (Nijut Moina Asoni - ছাত্ৰীৰ বাবে মাহেকীয়া সাহায্য)",
                body = """
                    📢 অসম চৰকাৰৰ মুখ্যমন্ত্ৰী নিজুত মইনা আঁচনি ২০২৬ (MMNMA Asoni)
                    
                    বাল্যবিবাহ ৰোধ আৰু ছাত্ৰীসকলক উচ্চ শিক্ষাৰ বাবে উৎসাহিত কৰিবলৈ অসম চৰকাৰৰ ঐতিহাসিক আঁচনি।
                    
                    📌 সাহায্যৰ পৰিমাণ (Financial Grant):
                    • উচ্চতৰ মাধ্যমিক (১১শ আৰু ১২শ শ্ৰেণী): প্ৰতি মাহে ₹১,০০০/-
                    • স্নাতক শ্ৰেণী (BA / BSc / BCom / Professional): প্ৰতি মাহে ₹১,২৫০/-
                    • স্নাতকোত্তৰ শ্ৰেণী (MA / MSc / MCom / B.Ed): প্ৰতি মাহে ₹২,৫০০/-
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. অসমৰ চৰকাৰী আৰু প্ৰাদেশিকীকৃত মহাবিদ্যালয়/বিশ্ববিদ্যালয়ত নিয়মীয়া ছাত্ৰী হ'ব লাগিব।
                    ২. ক্লাছত ন্যূনতম ৭৫% উপস্থিতি থকা বাধ্যতামূলক।
                    ৩. অনুশাসন মানি চলা আৰু পৰীক্ষাত নিয়মীয়াকৈ অৱতীৰ্ণ হোৱাটো বাধ্যতামূলক।
                    ৪. একাদশ আৰু স্নাতক স্তৰত বিবাহিত ছাত্ৰী এই আঁচনিৰ বাবে যোগ্য নহয় (স্নাতকোত্তৰ আৰু বিএডৰ ছাত্ৰীৰ বাবে ৰেহাই আছে)।
                    ৫. প্ৰয়োজনীয় নথিপত্ৰ:
                       - ছাত্ৰীগৰাকীৰ নামত সক্ৰিয় বেংক একাউণ্ট (আধাৰ সংযুক্ত)।
                       - মহাবিদ্যালয়ত নামভৰ্তিৰ মাচুলৰ ৰচিদ।
                       - আধাৰ কাৰ্ড আৰু ৰেচন কাৰ্ড।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    অসম চৰকাৰৰ উচ্চ শিক্ষা বিভাগৰ অফিচিয়েল প'ৰ্টেলত আবেদন কৰক:
                    👉 https://mmnma.assam.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ৩১ ডিচেম্বৰ ২০২৬
                """.trimIndent(),
                caption = "মুখ্যমন্ত্ৰী নিজুত মইনা আঁচনি! ছাত্ৰীসকলক মাহে ₹১,০০০ ৰ পৰা ₹২,৫০০ সাহায্য। ক'ত আবেদন কৰিব চাওক।",
                hashtags = "#NijutMoina, #AssamGovt, #অসম_চৰকাৰ, #HimantaBiswaSarma, #GirlEducation, #NijutMoinaAsoni",
                sourceUrl = "https://mmnma.assam.gov.in",
                sourceName = "Higher Education Department, Government of Assam",
                createdAt = now - 4000,
                updatedAt = now - 4000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 6. Orunodoi 3.0 Scheme
            ContentEntity(
                id = "post_orunodoi_3_scheme_2026",
                sourceOpportunityId = "orunodoi_assam_gov",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "অৰুণোদয় ৩.০ আঁচনি (Orunodoi 3.0 - মহিলাৰ একাউণ্টত মাহে ১২৫০ টকা)",
                body = """
                    📢 অসম চৰকাৰৰ অৰুণোদয় ৩.০ আঁচনি (Orunodoi 3.0 Scheme)
                    
                    অসমৰ ৩০ লাখতকৈ অধিক আৰ্থিকভাৱে পিছপৰা পৰিয়ালৰ মহিলাসকলক স্বাৱলম্বী কৰাৰ বাবে মাহেকীয়া আৰ্থিক অনুদান।
                    
                    📌 সাহায্যৰ পৰিমাণ (Financial Assistance):
                    প্ৰতি মাহৰ ১০ তাৰিখে মহিলা হিতাধিকাৰীৰ বেংক একাউণ্টত পোনে পোনে ₹১,২৫০/- টকা ডিবিটি (DBT) যোগে জমা কৰা হয়।
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. অসমৰ স্থায়ী বাসিন্দা মহিলা হ'ব লাগিব।
                    ২. পৰিয়ালৰ বাৰ্ষিক আয় ₹২,০০,০০০/- (২ লাখ টকা) তকৈ কম হ'ব লাগিব।
                    ৩. পৰিয়ালত ৰেচন কাৰ্ড (NFSA Ration Card) থকাটো বাধ্যতামূলক।
                    ৪. বিধৱা, অবিবাহিতা মহিলা, দিব্যাংগ সদস্য থকা পৰিয়াল আৰু জটিল ৰোগত আক্ৰান্ত পৰিয়ালক অগ্ৰাধিকাৰ দিয়া হ'ব।
                    ৫. পৰিয়ালৰ কোনো সদস্য চৰকাৰী কৰ্মচাৰী বা আয়কৰদাতা হ'ব নোৱাৰিব।
                    ৬. প্ৰয়োজনীয় নথিপত্ৰ:
                       - আধাৰ কাৰ্ড আৰু ৰেচন কাৰ্ড।
                       - মহিলাগৰাকীৰ একক বেংক একাউণ্টৰ পাছবুক।
                       - পৰিয়ালৰ আয়ৰ প্ৰমাণপত্ৰ।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    অৰুণোদয়ৰ অফিচিয়েল পৰ্টেল আৰু স্থানীয় পঞ্চায়ত / পৌৰসভা কাৰ্যালয়ত যোগাযোগ কৰক:
                    👉 https://orunodoi.assam.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ৩১ মাৰ্চ ২০২৭
                """.trimIndent(),
                caption = "অৰুণোদয় ৩.০ আঁচনি! প্ৰতি মাহে ১২৫০ টকা পোনে পোনে বেংক একাউণ্টত। সকলো সবিশেষ ইয়াত পঢ়ক।",
                hashtags = "#Orunodoi3, #OrunodoiAssam, #অৰুণোদয়, #AssamGovtScheme, #WomenEmpowerment, #AssamAsoni",
                sourceUrl = "https://orunodoi.assam.gov.in",
                sourceName = "Finance Department, Government of Assam",
                createdAt = now - 5000,
                updatedAt = now - 5000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 7. CMAAA 2.0
            ContentEntity(
                id = "post_cmaaa_atmanirbhar_2026",
                sourceOpportunityId = "cmaaa_assam_gov",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "মুখ্যমন্ত্ৰী আত্মনিৰ্ভৰ অসম অভিযান ২.০ (CMAAA - যুৱক-যুৱতীক ২ ৰ পৰা ৫ লাখ সাহায্য)",
                body = """
                    📢 মুখ্যমন্ত্ৰী আত্মনিৰ্ভৰ অসম অভিযান ২.০ (CMAAA 2.0)
                    
                    নিবনুৱা সমস্যা সমাধান আৰু অসমৰ যুৱ প্ৰজন্মক উদ্যোগী কৰি গঢ়ি তুলিবলৈ অসম চৰকাৰৰ অভিলাষী আঁচনি।
                    
                    📌 সাহায্যৰ পৰিমাণ (Assistance Amount):
                    • সাধাৰণ শাখা (১০ম/১২শ/স্নাতক উত্তীৰ্ণ): ₹২,০০,০০০/- (₹১ লাখ চৰকাৰী অনুদান + ₹১ লাখ সুতমুক্ত বেংক ঋণ)
                    • পেছাদাৰী শাখা (Engineering, MBBS, BDS, Agriculture): ₹৫,০০,০০০/- (₹২.৫ লাখ অনুদান + ₹২.৫ লাখ ঋণ)
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. অসমৰ স্থায়ী বাসিন্দা হ'ব লাগিব।
                    ২. বয়স: ২৮ ৰ পৰা ৪০ বছৰ (SC/ST/OBC ৰ বাবে ৪৩ বছৰলৈকে ৰেহাই)।
                    ৩. অসমৰ এমপ্লয়মেণ্ট এক্সচেঞ্জত পঞ্জীয়ন থকা বাধ্যতামূলক।
                    ৪. পূৰ্বৰ কোনো বেংক ঋণ খেলাপি (Defaulter) হ'ব নোৱাৰিব।
                    ৫. প্ৰয়োজনীয় নথিপত্ৰ:
                       - এমপ্লয়মেণ্ট এক্সচেঞ্জ কাৰ্ড
                       - শিক্ষাগত অৰ্হতাৰ প্ৰমাণপত্ৰ
                       - ব্যৱসায়িক প্ৰকল্পৰ খচৰা (Project Report)
                       - বেংক একাউণ্ট আৰু আধাৰ কাৰ্ড
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    অনলাইন প'ৰ্টেলৰ জৰিয়তে পোনপটীয়া আবেদন কৰক:
                    👉 https://cmaaa.assam.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ৩১ জানুৱাৰী ২০২৭
                """.trimIndent(),
                caption = "আত্মনিৰ্ভৰ অসম অভিযান ২.০! যুৱক-যুৱতীক ২ ৰ পৰা ৫ লাখ টকাৰ অনুদান আৰু ঋণ। কেনেকৈ আবেদন কৰিব চাওক।",
                hashtags = "#AtmanirbharAsom, #CMAAA, #AssamYouth, #SelfEmployment, #অসম_চৰকাৰ, #AssamStartup",
                sourceUrl = "https://cmaaa.assam.gov.in",
                sourceName = "Department of Industries & Commerce, Government of Assam",
                createdAt = now - 6000,
                updatedAt = now - 6000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 8. Pragyan Bharati Scooty Scheme
            ContentEntity(
                id = "post_pragyan_bharati_scooty_2026",
                sourceOpportunityId = "scooty_scheme_assam",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "প্ৰজ্ঞান ভাৰতী স্কুটাৰ আঁচনি ২০২৬ (Dr. Banikanta Kakati Merit Award)",
                body = """
                    🛵 প্ৰজ্ঞান ভাৰতী স্কুটাৰ আঁচনি ২০২৬ (Pragyan Bharati Scooty Scheme)
                    
                    উচ্চতৰ মাধ্যমিক চূড়ান্ত পৰীক্ষাত উজ্জ্বল ফলাফল দেখুওৱা ছাত্ৰ-ছাত্ৰীসকলক অসম চৰকাৰৰ তৰফৰ পৰা বিনামূলীয়া স্কুটাৰ প্ৰদান।
                    
                    📌 পুৰস্কাৰৰ সবিশেষ (Award Details):
                    ড° বাণীকান্ত কাকতি মেধা বঁটাৰ অধীনত পেট্ৰ'ল চালিত স্কুটাৰ অথবা ইলেকট্ৰিক স্কুটাৰ (ই-স্কুটাৰ) ছাত্ৰ-ছাত্ৰীৰ পচন্দ অনুসৰি প্ৰদান কৰা হয়।
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. অসম উচ্চতৰ মাধ্যমিক শিক্ষা সংসদৰ (AHSEC) অধীনত অনুষ্ঠিত চূড়ান্ত পৰীক্ষাত উত্তীৰ্ণ হ'ব লাগিব।
                    ২. মেধাৰ মাপকাঠি:
                       - ছাত্ৰীসকলৰ বাবে: ন্যূনতম ৬০% নম্বৰ (প্ৰথম বিভাগ)।
                       - ছাত্ৰসকলৰ বাবে: ন্যূনতম ৭৫% নম্বৰ (ষ্টাৰ মাৰ্কছ)।
                    ৩. অসমৰ চৰকাৰী অথবা প্ৰাদেশিকীকৃত মহাবিদ্যালয়ৰ পৰা পৰীক্ষাত অৱতীৰ্ণ হোৱা শিক্ষাৰ্থী।
                    ৪. প্ৰয়োজনীয় নথিপত্ৰ:
                       - উচ্চতৰ মাধ্যমিকৰ এডমিট কাৰ্ড আৰু মাৰ্কশ্বীট
                       - ছাত্ৰ-ছাত্ৰীৰ আধাৰ কাৰ্ড আৰু ফটো
                       - মহাবিদ্যালয়ৰ অধ্যক্ষৰ প্ৰত্যয়ন পত্ৰ
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    উচ্চ শিক্ষা সঞ্চালকালয়ৰ প'ৰ্টেলত পঞ্জীয়ন কৰক:
                    👉 https://directorateofhighereducation.assam.gov.in
                    
                    ⏰ আবেদনৰ অন্তিম তাৰিখ: ৩১ ডিচেম্বৰ ২০২৬
                """.trimIndent(),
                caption = "প্ৰজ্ঞান ভাৰতী স্কুটাৰ আঁচনি! মেধাৱী ছাত্ৰ-ছাত্ৰীক বিনামূলীয়া স্কুটাৰ। পঞ্জীয়ন প্ৰক্ৰিয়া আৰম্ভ।",
                hashtags = "#PragyanBharati, #AssamScootyScheme, #BanikantaKakatiAward, #AHSEC, #StudentIncentive, #AssamEducation",
                sourceUrl = "https://directorateofhighereducation.assam.gov.in",
                sourceName = "Directorate of Higher Education, Government of Assam",
                createdAt = now - 7000,
                updatedAt = now - 7000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 9. Swanirbhar Nari Asoni
            ContentEntity(
                id = "post_swanirbhar_nari_2026",
                sourceOpportunityId = "swanirbhar_nari_assam",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "স্বনিৰ্ভৰ নাৰী আঁচনি ২০২৬ (Swanirbhar Nari - শিপিনী মহিলাৰ পৰা পোনপটীয়া হস্ততাঁত ক্ৰয়)",
                body = """
                    📢 অসম চৰকাৰৰ স্বনিৰ্ভৰ নাৰী আঁচনি ২০২৬ (Swanirbhar Nari Asoni)
                    
                    অসমৰ থলুৱা শিপিনী মহিলাসকলক অৰ্থনৈতিকভাৱে স্বাৱলম্বী কৰাৰ উদ্দেশ্যে মধ্যভোগী অবিহনে হস্ততাঁত বস্ত্ৰ পোনপটীয়াকৈ চৰকাৰে ক্ৰয় কৰাৰ ব্যৱস্থা।
                    
                    📌 সাহায্য আৰু সুবিধা (Benefits):
                    • শিপিনীসকলৰ তৈয়াৰী গামোচা, চাদৰ, মেখেলা, আৰনাই আদি ৩১ বিধ পৰম্পৰাগত হস্ততাঁত সামগ্ৰী নিৰ্ধাৰিত চৰকাৰী মূল্যত ক্ৰয়।
                    • টকা পোনে পোনে শিপিনীৰ বেংক একাউণ্টত জমা।
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. অসমৰ থলুৱা মহিলা শিপিনী হ'ব লাগিব।
                    ২. নিজৰ তাঁতশাল (Handloom) থকা বাধ্যতামূলক।
                    ৩. স্বনিৰ্ভৰ নাৰী প'ৰ্টেলত পঞ্জীয়ন থকা শিপিনী কাৰ্ড (Weaver Card) থাকিব লাগিব।
                    ৪. প্ৰয়োজনীয় নথিপত্ৰ:
                       - আধাৰ কাৰ্ড আৰু ভোটাৰ কাৰ্ড।
                       - বেংক একাউণ্টৰ পাছবুক।
                       - তাঁতশালৰ সৈতে শিপিনীৰ ফটো।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    হস্ততাঁত আৰু বস্ত্ৰশিল্প বিভাগৰ অফিচিয়েল প'ৰ্টেলত পঞ্জীয়ন কৰক:
                    👉 https://swanirbharnaari.assam.gov.in
                    
                    ⏰ পঞ্জীয়ন আৰু ক্ৰয় প্ৰক্ৰিয়া: চলি আছে (Active 2026-2027)
                """.trimIndent(),
                caption = "স্বনিৰ্ভৰ নাৰী আঁচনি! শিপিনীৰ পৰা পোনে পোনে চৰকাৰে ক্ৰয় কৰিব কাপোৰ। পঞ্জীয়নৰ সবিশেষ চাওক।",
                hashtags = "#SwanirbharNari, #AssamWeavers, #স্বনিৰ্ভৰ_নাৰী, #AssamHandloom, #WomenEmpowerment, #AssamAsoni",
                sourceUrl = "https://swanirbharnaari.assam.gov.in",
                sourceName = "Handloom, Textiles & Sericulture Dept, Government of Assam",
                createdAt = now - 8000,
                updatedAt = now - 8000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            ),

            // 10. Arundhati Gold Scheme
            ContentEntity(
                id = "post_arundhati_gold_2026",
                sourceOpportunityId = "arundhati_gold_assam",
                contentType = ContentType.OPPORTUNITY_POST.name,
                platform = ContentPlatform.FACEBOOK.name,
                title = "অৰুন্ধতী সোণ আঁচনি ২০২৬ (Arundhati Gold Scheme - ন-কইনাক ৪০ হাজাৰ টকাৰ সোণ)",
                body = """
                    📢 অসম চৰকাৰৰ অৰুন্ধতী সোণ আঁচনি ২০২৬ (Arundhati Gold Scheme)
                    
                    অৰ্থনৈতিকভাৱে পিছপৰা পৰিয়ালৰ কন্যা সন্তানৰ বিবাহৰ সময়ত ১ তোলা (১০ গ্ৰাম) সোণ ক্ৰয়ৰ বাবে চৰকাৰী আৰ্থিক সাহায্য।
                    
                    📌 সাহায্যৰ পৰিমাণ (Financial Assistance):
                    বিবাহ পঞ্জীয়নৰ পাছত কইনাৰ বেংক একাউণ্টত পোনে পোনে ₹৪০,০০০/- টকা জমা কৰা হয়।
                    
                    🎯 কি কি যোগ্যতা লাগিব (Requirements / Eligibility):
                    ১. কন্যাগৰাকী অসমৰ স্থায়ী বাসিন্দা হ'ব লাগিব।
                    ২. কইনাৰ বয়স ন্যূনতম ১৮ বছৰ আৰু দৰাৰ বয়স ন্যূনতম ২১ বছৰ হ'ব লাগিব।
                    ৩. বিশেষ বিবাহ আইন ১৯৫৪ (Special Marriage Act) ৰ অধীনত আনুষ্ঠানিকভাৱে বিবাহ পঞ্জীয়ন কৰোৱা বাধ্যতামূলক।
                    ৪. পৰিয়ালৰ বাৰ্ষিক আয় ৫ লাখ টকাতকৈ কম হ'ব লাগিব।
                    ৫. প্ৰয়োজনীয় নথিপত্ৰ:
                       - বিশেষ বিবাহ আইনৰ অধীনত বিবাহ পঞ্জীয়নৰ প্ৰমাণপত্ৰ।
                       - বয়সৰ প্ৰমাণপত্ৰ (HSLC এডমিট বা জন্মৰ প্ৰমাণপত্ৰ)।
                       - পৰিয়ালৰ বাৰ্ষিক আয়ৰ প্ৰমাণপত্ৰ।
                       - কইনাৰ একক বেংক একাউণ্টৰ পাছবুক।
                    
                    🔗 ক'ত আবেদন কৰিব (Where to Apply / Official Portal):
                    ৰাজহ আৰু দুৰ্যোগ ব্যৱস্থাপনা বিভাগৰ অফিচিয়েল প'ৰ্টেলত আবেদন কৰক:
                    👉 https://revenue.assam.gov.in
                    
                    ⏰ আবেদন সময়সীমা: বিবাহ পঞ্জীয়নৰ দিনাই অথবা নিৰ্ধাৰিত সময়ৰ ভিতৰত
                """.trimIndent(),
                caption = "অৰুন্ধতী সোণ আঁচনি! ন-কইনাক ১ তোলা সোণৰ বাবে ৪০,০০০ টকাৰ সাহায্য। কেনেকৈ আবেদন কৰিব চাওক।",
                hashtags = "#ArundhatiGoldScheme, #AssamGovt, #অৰুন্ধতী, #MarriageAssistance, #AssamSchemes, #WomenWelfare",
                sourceUrl = "https://revenue.assam.gov.in",
                sourceName = "Revenue & Disaster Management Dept, Government of Assam",
                createdAt = now - 9000,
                updatedAt = now - 9000,
                generationStatus = GenerationStatus.APPROVED.name,
                verificationStatus = VerificationStatus.VERIFIED.name,
                isSourceVerified = true
            )
        )
    }
}
