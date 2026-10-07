package com.example.data.remote.scout

import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Official Assam Government Schemes (অসম চৰকাৰৰ আঁচনি / Asoni) Source Provider.
 * Covers all flagship citizen welfare, youth employment, student incentive, and women empowerment schemes in Assam.
 */
class AssamGovernmentSchemesSourceProvider : SourceProvider {
    override val providerId: String = "assam_gov_schemes_asoni"
    override val providerName: String = "Assam Government Flagship Schemes (Asoni)"
    override val sourceTier: SourceTier = SourceTier.TIER_1_OFFICIAL
    override val defaultRegion: OpportunityRegion = OpportunityRegion.ASSAM

    override suspend fun fetchItems(): List<OpportunityRawItem> = withContext(Dispatchers.IO) {
        listOf(
            OpportunityRawItem(
                title = "মুখ্যমন্ত্ৰী নিজুত মইনা আঁচনি ২০২৬ (MMNMA - ছাত্ৰীৰ মাহেকীয়া আৰ্থিক অনুদান)",
                description = "অসম চৰকাৰৰ উচ্চ শিক্ষাৰ ঐতিহাসিক আঁচনি: একাদশ-দ্বাদশ শ্ৰেণীত মাহে ₹১,০০০, স্নাতক শ্ৰেণীত মাহে ₹১,২৫০, আৰু স্নাতকোত্তৰ/বিএডত মাহে ₹২,৫০০। ক'ত আবেদন কৰিব: https://mmnma.assam.gov.in । কি কি লাগিব: চৰকাৰী মহাবিদ্যালয়ৰ নিয়মীয়া ছাত্ৰী, ৭৫% উপস্থিতি, বেংক একাউণ্ট, আধাৰ কাৰ্ড।",
                sourceName = "Higher Education Dept, Govt of Assam",
                sourceUrl = "https://mmnma.assam.gov.in",
                publishedAt = "2026-10-01",
                deadline = "31/12/2026",
                eligibility = "Girl students enrolled in government and provincialized colleges/universities in Assam; 75% attendance mandatory; Aadhaar linked bank account.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "অৰুণোদয় ৩.০ আঁচনি (Orunodoi 3.0 - মহিলাৰ বাবে মাহেকীয়া ১২৫০ টকা ডিবিটি)",
                description = "অসমৰ সৰ্ববৃহৎ ডিবিটি আঁচনি: প্ৰতি মাহৰ ১০ তাৰিখে মহিলা হিতাধিকাৰীৰ বেংক একাউণ্টত পোনে পোনে ₹১,২৫০ টকা। ক'ত আবেদন কৰিব: https://orunodoi.assam.gov.in । কি কি লাগিব: অসমৰ স্থায়ী বাসিন্দা মহিলা, পৰিয়ালৰ বাৰ্ষিক আয় ২ লাখ টকাতকৈ কম, ৰেচন কাৰ্ড, আধাৰ কাৰ্ড।",
                sourceName = "Finance Department, Govt of Assam",
                sourceUrl = "https://orunodoi.assam.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/03/2027",
                eligibility = "Permanent resident women of Assam; family income < ₹2 Lakhs; active NFSA ration card; individual bank passbook.",
                organization = "Finance Department, Government of Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "মুখ্যমন্ত্ৰী আত্মনিৰ্ভৰ অসম অভিযান ২.০ (CMAAA - যুৱক-যুৱতীক ২ ৰ পৰা ৫ লাখ সাহায্য)",
                description = "নিবনুৱা যুৱক-যুৱতীৰ স্বনিয়োজনৰ বাবে ₹২ লাখ (১ লাখ অনুদান + ১ লাখ সুতমুক্ত ঋণ) আৰু পেছাদাৰী স্নাতকৰ বাবে ₹৫ লাখ সাহায্য। ক'ত আবেদন কৰিব: https://cmaaa.assam.gov.in । কি কি লাগিব: বয়স ২৮-৪০ বছৰ, এমপ্লয়মেণ্ট এক্সচেঞ্জ পঞ্জীয়ন কাৰ্ড, প্ৰকল্প প্ৰতিবেদন।",
                sourceName = "Department of Industries & Commerce, Assam",
                sourceUrl = "https://cmaaa.assam.gov.in",
                publishedAt = "2026-09-30",
                deadline = "31/01/2027",
                eligibility = "Unemployed youth aged 28-40 yrs (relaxed to 43 for SC/ST/OBC); registered in Assam Employment Exchange; viable project report.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "মুখ্যমন্ত্ৰী মহিলা উদ্যোগিতা আঁচনি (MMUA - ৩৫,০০০ টকাৰ মহিলা উদ্যোগী সাহায্য)",
                description = "আত্মসহায়ক গোটৰ (SHG) মহিলাৰ বাবে আৰ্থিক সাহায্য: প্ৰথম বছৰত ₹১০,০০০ অনুদান আৰু দ্বিতীয় বছৰত ₹২৫,০০০ (১২,৫০০ অনুদান + ১২,৫০০ বেংক ঋণ)। ক'ত আবেদন কৰিব: https://mmua.assam.gov.in । কি কি লাগিব: অসমৰ পঞ্জীয়নভুক্ত গাঁৱলীয়া SHG ৰ সক্ৰিয় সদস্য।",
                sourceName = "Panchayat & Rural Development, Assam",
                sourceUrl = "https://mmua.assam.gov.in",
                publishedAt = "2026-10-01",
                deadline = "28/02/2027",
                eligibility = "Active women members of registered rural SHGs in Assam with enterprise plan and complying with child norms.",
                organization = "ASRLM, Government of Assam",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "প্ৰজ্ঞান ভাৰতী স্কুটাৰ আঁচনি ২০২৬ (Dr. Banikanta Kakati Merit Award)",
                description = "উচ্চতৰ মাধ্যমিক চূড়ান্ত পৰীক্ষাত উত্তীৰ্ণ মেধাৱী ছাত্ৰ-ছাত্ৰীক অসম চৰকাৰৰ বিনামূলীয়া স্কুটাৰ। ক'ত আবেদন কৰিব: https://directorateofhighereducation.assam.gov.in । কি কি লাগিব: ছাত্ৰীৰ বাবে ৬০% নম্বৰ আৰু ছাত্ৰৰ বাবে ৭৫% নম্বৰ, AHSEC মাৰ্কশ্বীট আৰু এডমিট কাৰ্ড।",
                sourceName = "Directorate of Higher Education, Assam",
                sourceUrl = "https://directorateofhighereducation.assam.gov.in",
                publishedAt = "2026-09-28",
                deadline = "31/12/2026",
                eligibility = "AHSEC Class 12 passed from govt/provincialized colleges: Girls with 60%+ marks, Boys with 75%+ marks; pass certificate.",
                organization = "Higher Education Department, Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "প্ৰজ্ঞান ভাৰতী বিনামূলীয়া নামভৰ্তি আঁচনি (Assam Free Admission & Textbooks)",
                description = "অসমৰ চৰকাৰী আৰু প্ৰাদেশিকীকৃত মহাবিদ্যালয়ত উচ্চতৰ মাধ্যমিক, ডিগ্ৰী আৰু স্নাতকোত্তৰ স্তৰত ১০০% বিনামূলীয়া নামভৰ্তি। ক'ত আবেদন কৰিব: https://directorateofhighereducation.assam.gov.in । কি কি লাগিব: পৰিয়ালৰ বাৰ্ষিক আয় ২ লাখ টকাতকৈ কম, এখন গছৰ পুলি ৰোপণৰ ফটো।",
                sourceName = "Directorate of Higher Education, Assam",
                sourceUrl = "https://directorateofhighereducation.assam.gov.in",
                publishedAt = "2026-09-29",
                deadline = "31/01/2027",
                eligibility = "Annual parental income from all sources < ₹2.00 Lakhs; plantation of sapling photo submission required.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.EDUCATION,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Bhasha Gourab Asoni (ভাষা গৌৰৱ আঁচনি) - Corpus Grants for Indigenous Languages",
                description = "Assam Government one-time financial assistance of ₹50,000 to eminent indigenous authors and corpus fund allocation of ₹3 to ₹5 Crore to tribal Sahitya Sabhas.",
                sourceName = "Cultural Affairs Department, Assam",
                sourceUrl = "https://assam.gov.in",
                publishedAt = "2026-09-26",
                deadline = "15/01/2027",
                eligibility = "Writers, translators, and linguistic researchers in Bodo, Mising, Karbi, Dimasa, Tiwa, Rabha, and other indigenous languages.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Assam Microfinance Incentive and Relief Scheme (AMFIRS)",
                description = "Relief assistance incentive up to ₹25,000 for regular borrowers (Category I) and overdue loan repayment support (Category II & III) for female microfinance clients.",
                sourceName = "Finance Department, Assam",
                sourceUrl = "https://finance.assam.gov.in",
                publishedAt = "2026-09-27",
                deadline = "31/03/2027",
                eligibility = "Women borrowers who availed microfinance loans in Assam with income <= ₹1 Lakh per annum.",
                organization = "Finance Department, Govt of Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Mission Basundhara 3.0 - Digital Land Rights & Tenancy Settlement Portal",
                description = "Flagship land revenue initiative enabling online conversion of annual patta to periodic patta, settlement of occupancy tenants, and transparent online mutation.",
                sourceName = "Revenue & Disaster Management, Assam",
                sourceUrl = "https://basundhara.assam.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/03/2027",
                eligibility = "Indigenous landholders, hereditary cultivators, and tenants residing in Assam.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Assam Direct Recruitment 2.0 (ADRE) - 35,000 Grade III & IV Posts",
                description = "State-level recruitment commission competitive examination for 35,000+ vacancies across Assam Government departments for Grade 3 and Grade 4 administrative staff.",
                sourceName = "State Level Recruitment Commission (SLRC)",
                sourceUrl = "https://slrcg3.sebaonline.org",
                publishedAt = "2026-10-01",
                deadline = "20/01/2027",
                eligibility = "Class 8/10th pass for Grade IV; 10+2 or Graduation with Computer Diploma for Grade III; Assam employment exchange registration required.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Ayushman Asom - Mukhya Mantri Jan Arogya Yojana (MMJAY) Cashless Card",
                description = "Family floater health insurance coverage of ₹5 Lakhs per annum for cashless hospitalization across 300+ government and private empanelled hospitals in Assam.",
                sourceName = "State Health Agency, Assam",
                sourceUrl = "https://sha.assam.gov.in",
                publishedAt = "2026-09-30",
                deadline = "31/12/2027",
                eligibility = "All families in Assam having an active National Food Security Act (NFSA) Ration Card.",
                organization = "National Health Mission, Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Assam Startup The Nest Incubator Cohort - Grants up to ₹50 Lakhs",
                description = "Government of Assam incubation and seed grant initiative providing state-of-the-art co-working space, dedicated venture mentorship, and scale-up grants.",
                sourceName = "Assam Startup, Govt of Assam",
                sourceUrl = "https://startup.assam.gov.in",
                publishedAt = "2026-09-25",
                deadline = "15/02/2027",
                eligibility = "Early-stage startups registered in Assam or operating with primary business impact in Northeast India.",
                organization = "Industries & Commerce Department, Assam",
                categoryHint = OpportunityCategory.STARTUP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Swanirbhar Nari Asoni (স্বনিৰ্ভৰ নাৰী আঁচনি) - Direct Handloom Procurement from Women Weavers",
                description = "Assam Government portal for direct procurement of traditional handloom products directly from registered indigenous women weavers without middlemen, ensuring fair remunerative prices.",
                sourceName = "Handloom, Textiles & Sericulture Dept, Assam",
                sourceUrl = "https://swanirbharnaari.assam.gov.in",
                publishedAt = "2026-10-01",
                deadline = "31/03/2027",
                eligibility = "Indigenous registered female weavers possessing an active weaver card in Assam.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Arundhati Gold Scheme (অৰুন্ধতী সোণ আঁচনি) - Financial Aid for Newlywed Brides",
                description = "Financial assistance of ₹40,000 to purchase 1 tola (10 grams) of gold for newlywed brides from economically weaker sections upon formal registration of marriage.",
                sourceName = "Revenue & Disaster Management, Assam",
                sourceUrl = "https://revenue.assam.gov.in",
                publishedAt = "2026-09-28",
                deadline = "31/12/2027",
                eligibility = "Brides whose annual family income is below ₹5 Lakhs, with marriage formally registered under the Special Marriage Act 1954.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Abhinandan Education Loan Subsidy Scheme - ₹50,000 Subsidy for Higher Studies",
                description = "Government of Assam one-time subsidy of up to ₹50,000 on education loans sanctioned by commercial or regional rural banks for undergraduate/postgraduate degrees.",
                sourceName = "Finance Department, Assam",
                sourceUrl = "https://finance.assam.gov.in",
                publishedAt = "2026-09-29",
                deadline = "31/03/2027",
                eligibility = "Students who are permanent residents of Assam pursuing recognized higher education programs with active bank education loans.",
                organization = "Finance Department, Govt of Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Anundoram Borooah Award Scheme (ARBAS) - Free Laptops & Citations for HSLC Achievers",
                description = "Merit award conferring modern high-performance laptops or equivalent cash incentive of ₹20,000 along with certificates of appreciation to students securing 75%+ in HSLC/AHM exams.",
                sourceName = "Secondary Education Dept, Assam",
                sourceUrl = "https://arbas.assam.gov.in",
                publishedAt = "2026-09-26",
                deadline = "15/01/2027",
                eligibility = "Class 10 students passing HSLC examination conducted by SEBA with star marks or 75% aggregate marks.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Mukhya Mantri Krishi Sa-Sajuli Yojana - ₹5,000 Direct Farm Equipment Grant",
                description = "Financial assistance of ₹5,000 credited directly into the bank accounts of small and marginal farmers in Assam to purchase modern agricultural hand-tools and farm implements.",
                sourceName = "Directorate of Agriculture, Assam",
                sourceUrl = "https://diragri.assam.gov.in",
                publishedAt = "2026-10-02",
                deadline = "28/02/2027",
                eligibility = "Small and marginal farmers cultivating land in Assam with valid Kisan Credit Card (KCC) or Aadhaar-linked bank accounts.",
                organization = "Agriculture Department, Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Assam Police SLPRB - Direct Recruitment for 6,000+ Constables & Sub-Inspectors",
                description = "State Level Police Recruitment Board large-scale competitive hiring for Armed and Unarmed branch Constables, Sub-Inspectors (SI), and Commando Battalion jawans.",
                sourceName = "State Level Police Recruitment Board (SLPRB)",
                sourceUrl = "https://slprbassam.in",
                publishedAt = "2026-10-01",
                deadline = "31/01/2027",
                eligibility = "HSLC passed for Constable, Graduate for Sub-Inspector; Assam employment exchange registration required.",
                organization = "Assam Police, Govt of Assam",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.ASSAM
            )
        )
    }
}
