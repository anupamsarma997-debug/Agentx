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
                title = "Mukhya Mantri Nijut Moina Asoni (MMNMA) - Monthly Financial Grant for Girl Students",
                description = "Assam Government incentive scheme to stop child marriage and promote higher education: ₹1,000/month (HS 11-12), ₹1,250/month (Degree/BSc/BA/BCom), ₹2,500/month (Post-Graduation/BEd).",
                sourceName = "Higher Education Dept, Govt of Assam",
                sourceUrl = "https://mmnma.assam.gov.in",
                publishedAt = "2026-10-01",
                deadline = "31/12/2026",
                eligibility = "Girl students enrolled in government and provincialized colleges/universities in Assam; maintaining minimum 75% attendance.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Orunodoi 3.0 Scheme (অৰুণোদয় ৩.০) - ₹1,250 Monthly Direct Benefit Transfer",
                description = "Assam's largest direct benefit transfer (DBT) scheme providing ₹1,250 on the 10th of every month directly into the bank accounts of 30+ Lakh beneficiary women.",
                sourceName = "Finance Department, Govt of Assam",
                sourceUrl = "https://orunodoi.assam.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/03/2027",
                eligibility = "Permanent resident women of Assam from low-income households (annual family income < ₹2 Lakhs); ration card holders prioritized.",
                organization = "Finance Department, Government of Assam",
                categoryHint = OpportunityCategory.GRANT,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Mukhya Mantri Atmanirbhar Asom Abhijan 2.0 (CMAAA) - ₹2 Lakh to ₹5 Lakh Youth Grant & Loan",
                description = "Youth self-employment flagship initiative providing financial assistance of ₹2 Lakhs (₹1 Lakh grant + ₹1 Lakh interest-free loan) and ₹5 Lakhs for professional graduates to set up ventures.",
                sourceName = "Department of Industries & Commerce, Assam",
                sourceUrl = "https://cmaaa.assam.gov.in",
                publishedAt = "2026-09-30",
                deadline = "31/01/2027",
                eligibility = "Unemployed youth of Assam aged 28-40 years (relaxed to 43 years for SC/ST/OBC); registered in Employment Exchange.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Mukhyamantri Mahila Udyamita Asoni (MMUA) - ₹35,000 Grant & Loan for SHG Women",
                description = "Assam State Rural Livelihoods Mission (ASRLM) enterprise scheme: ₹10,000 initial grant in Year 1, followed by ₹25,000 (₹12,500 grant + ₹12,500 bank loan) upon successful utilization.",
                sourceName = "Panchayat & Rural Development, Assam",
                sourceUrl = "https://mmua.assam.gov.in",
                publishedAt = "2026-10-01",
                deadline = "28/02/2027",
                eligibility = "Active women members of registered rural Self-Help Groups (SHGs) in Assam with a viable enterprise business plan.",
                organization = "ASRLM, Government of Assam",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Pragyan Bharati Scooty Scheme (Dr. Banikanta Kakati Merit Award)",
                description = "Free two-wheeler (Scooty) awarded to meritorious students of Assam: Girl students securing 60%+ and Boy students securing 75%+ in Higher Secondary examinations.",
                sourceName = "Directorate of Higher Education, Assam",
                sourceUrl = "https://directorateofhighereducation.assam.gov.in",
                publishedAt = "2026-09-28",
                deadline = "31/12/2026",
                eligibility = "Students who passed Class 12 (HS) from AHSEC recognized government/provincialized institutions with qualifying cutoff marks.",
                organization = "Higher Education Department, Assam",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Pragyan Bharati Free Admission & Free Textbook Scheme",
                description = "Complete waiver of admission and tuition fees in government, provincialized, and constituent colleges for HS 1st year, Degree, and Post-Graduate admissions.",
                sourceName = "Directorate of Higher Education, Assam",
                sourceUrl = "https://directorateofhighereducation.assam.gov.in",
                publishedAt = "2026-09-29",
                deadline = "31/01/2027",
                eligibility = "Students whose parental annual income from all sources is less than ₹2.00 Lakhs; plantation of a sapling required.",
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
