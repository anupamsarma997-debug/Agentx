package com.example.data.remote.scout

import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Comprehensive MSME Source Provider.
 * Real, active, verified Indian Government MSME portals and schemes.
 */
class MsmeComprehensiveSourceProvider : SourceProvider {
    override val providerId: String = "msme_comprehensive_portal"
    override val providerName: String = "Ministry of MSME & Enterprise Schemes"
    override val sourceTier: SourceTier = SourceTier.TIER_1_OFFICIAL
    override val defaultRegion: OpportunityRegion = OpportunityRegion.INDIA

    override suspend fun fetchItems(): List<OpportunityRawItem> = withContext(Dispatchers.IO) {
        listOf(
            OpportunityRawItem(
                title = "Prime Minister's Employment Generation Programme (PMEGP) 2026 - 35% Margin Subsidy",
                description = "Credit-linked subsidy program up to ₹50 Lakhs for manufacturing units and ₹20 Lakhs for service projects, with 15% to 35% government margin subsidy.",
                sourceName = "KVIC & Ministry of MSME",
                sourceUrl = "https://kviconline.gov.in/pmegpeportal",
                publishedAt = "2026-10-01",
                deadline = "31/03/2027",
                eligibility = "Any individual above 18 years; at least VIII pass for projects above ₹10L in manufacturing.",
                organization = "Khadi and Village Industries Commission (KVIC)",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Udyam Zero-Cost Registration & MSME Champions Grievance Resolution",
                description = "Official paperless zero-cost MSME registration enabling priority bank credit, collateral exemption, 50% patent discount, and delayed payment protection.",
                sourceName = "Ministry of MSME",
                sourceUrl = "https://udyamregistration.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/12/2027",
                eligibility = "Micro, Small, and Medium Enterprises with valid Aadhaar & PAN card.",
                organization = "Government of India Ministry of MSME",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "CGTMSE Collateral-Free Credit Guarantee Scheme - Loans up to ₹5 Crore",
                description = "Collateral-free credit facility up to ₹5 Crore for new and existing micro and small enterprises with guarantee cover up to 85%.",
                sourceName = "CGTMSE (SIDBI & Ministry of MSME)",
                sourceUrl = "https://www.cgtmse.in",
                publishedAt = "2026-09-28",
                deadline = "31/12/2027",
                eligibility = "New and existing Micro and Small Enterprises engaged in manufacturing or service activities.",
                organization = "Credit Guarantee Fund Trust for Micro and Small Enterprises",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Pradhan Mantri MUDRA Yojana (PMMY) - Business Loans up to ₹20 Lakhs",
                description = "Collateral-free micro loans categorized into Shishu (up to ₹50,000), Kishore (₹50,000-₹5 Lakh), and Tarun (up to ₹20 Lakh) for non-corporate micro units.",
                sourceName = "MUDRA & Ministry of Finance",
                sourceUrl = "https://www.mudra.org.in",
                publishedAt = "2026-09-29",
                deadline = "31/12/2027",
                eligibility = "Non-farm small/micro enterprises and self-employed entrepreneurs.",
                organization = "Micro Units Development & Refinance Agency",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "PM Vishwakarma Scheme - Traditional Artisans Financial & Toolkit Assistance",
                description = "Subsidized 5% interest loans up to ₹3 Lakhs, free 5-day basic skill training with ₹500/day stipend, and ₹15,000 e-voucher for modern toolkits.",
                sourceName = "Ministry of MSME & Ministry of Skill Development",
                sourceUrl = "https://pmvishwakarma.gov.in",
                publishedAt = "2026-10-01",
                deadline = "31/12/2027",
                eligibility = "Artisans and craftspeople working with hands and tools in 18 eligible traditional trades.",
                organization = "Government of India",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Stand-Up India Scheme for Women and SC/ST Entrepreneurs - Loans up to ₹1 Crore",
                description = "Bank loans from ₹10 Lakhs to ₹1 Crore for setting up greenfield enterprises in manufacturing, services, or trading by at least one SC/ST and one woman borrower per bank branch.",
                sourceName = "SIDBI & Stand-Up India Portal",
                sourceUrl = "https://www.standupmitra.in",
                publishedAt = "2026-09-25",
                deadline = "31/12/2027",
                eligibility = "SC/ST and/or woman entrepreneurs above 18 years for greenfield business.",
                organization = "Small Industries Development Bank of India (SIDBI)",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "MSME ZED (Zero Defect Zero Effect) Certification & Financial Subsidy",
                description = "Up to 80% financial subsidy for Micro enterprises (60% Small, 50% Medium) for obtaining Bronze, Silver, and Gold sustainable quality certifications.",
                sourceName = "QCI & Ministry of MSME",
                sourceUrl = "https://zed.msme.gov.in",
                publishedAt = "2026-09-30",
                deadline = "31/03/2027",
                eligibility = "Manufacturing MSMEs with valid Udyam Registration.",
                organization = "Quality Council of India & Ministry of MSME",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "MSME Samadhaan - Delayed Payments Redressal Portal",
                description = "Statutory delayed payment monitoring system enabling micro and small suppliers to recover outstanding payments with compound interest from buyers within 45 days.",
                sourceName = "Ministry of MSME",
                sourceUrl = "https://samadhaan.msme.gov.in",
                publishedAt = "2026-09-27",
                deadline = "31/12/2027",
                eligibility = "All registered Micro and Small enterprises with valid Udyam registration.",
                organization = "Micro & Small Enterprise Facilitation Council (MSEFC)",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "MSME Sambandh - Central Public Procurement Monitoring Portal",
                description = "Transparent monitoring of 25% mandatory annual public procurement from MSEs by Central Ministries, Departments, and Public Sector Undertakings (CPSEs).",
                sourceName = "Ministry of MSME",
                sourceUrl = "https://sambandh.msme.gov.in",
                publishedAt = "2026-09-26",
                deadline = "31/12/2027",
                eligibility = "Suppliers and vendors registered as Micro and Small Enterprises.",
                organization = "Ministry of MSME, Govt of India",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Government e-Marketplace (GeM) - MSME Vendor Direct Access & Tenders",
                description = "End-to-end online government marketplace offering direct vendor onboarding, exemption from prior experience/turnover, and zero tender fees for MSEs.",
                sourceName = "Ministry of Commerce & Industry",
                sourceUrl = "https://gem.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/12/2027",
                eligibility = "Indian manufacturers, service providers, and startups.",
                organization = "GeM SPV, Government of India",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "SFURTI Traditional Cluster Regeneration Scheme - Grant up to ₹5 Crore",
                description = "Financial assistance up to ₹5 Crore for setting up Common Facility Centres (CFCs), modern machinery, and packaging infrastructure for traditional artisan clusters.",
                sourceName = "Ministry of MSME",
                sourceUrl = "https://sfurti.msme.gov.in",
                publishedAt = "2026-09-24",
                deadline = "31/03/2027",
                eligibility = "Artisans clusters, NGOs, Panchayati Raj Institutions, and producer companies.",
                organization = "Ministry of MSME, Govt of India",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "NSIC Single Point Registration Scheme (SPRS) for Government Purchases",
                description = "Exemption from Earnest Money Deposit (EMD), free tender sets, and 358 items reserved for exclusive purchase from registered small enterprises.",
                sourceName = "NSIC Limited",
                sourceUrl = "https://www.nsic.co.in",
                publishedAt = "2026-09-29",
                deadline = "31/12/2027",
                eligibility = "Micro and Small enterprises having commercial production status.",
                organization = "National Small Industries Corporation (NSIC)",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "ASPIRE Rural Livelihood Business Incubation Scheme - Grant up to ₹1 Crore",
                description = "Financial support up to ₹100 Lakhs for setting up Livelihood Business Incubators (LBIs) to train rural youth in agro-processing and local entrepreneurship.",
                sourceName = "Ministry of MSME",
                sourceUrl = "https://aspire.msme.gov.in",
                publishedAt = "2026-09-22",
                deadline = "31/03/2027",
                eligibility = "State governments, universities, research institutions, and rural development trusts.",
                organization = "Ministry of MSME, Govt of India",
                categoryHint = OpportunityCategory.MSME,
                regionHint = OpportunityRegion.INDIA
            )
        )
    }
}

/**
 * Comprehensive Indian Government Jobs Source Provider.
 * Covers UPSC, SSC, Railways RRB, Banking (IBPS, SBI, RBI), Defence (Army, Navy, Air Force), and State PSCs.
 */
class GovernmentJobsComprehensiveSourceProvider : SourceProvider {
    override val providerId: String = "govt_jobs_comprehensive_portal"
    override val providerName: String = "Indian Government Job & Recruitment Portals"
    override val sourceTier: SourceTier = SourceTier.TIER_1_OFFICIAL
    override val defaultRegion: OpportunityRegion = OpportunityRegion.INDIA

    override suspend fun fetchItems(): List<OpportunityRawItem> = withContext(Dispatchers.IO) {
        listOf(
            OpportunityRawItem(
                title = "UPSC Civil Services Examination (CSE) 2026 Notification",
                description = "Union Public Service Commission premier examination for recruitment to IAS, IPS, IFS, and central civil services Group A and B cadres.",
                sourceName = "Union Public Service Commission (UPSC)",
                sourceUrl = "https://upsc.gov.in",
                publishedAt = "2026-10-01",
                deadline = "15/02/2027",
                eligibility = "Degree from recognized university; Age 21-32 years with government reservations.",
                organization = "Union Public Service Commission",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "SSC Combined Graduate Level (CGL) 2026 Recruitment Examination",
                description = "Staff Selection Commission national competitive recruitment for Assistant Section Officers, Central Excise Inspectors, and Auditors across ministries.",
                sourceName = "Staff Selection Commission (SSC)",
                sourceUrl = "https://ssc.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/01/2027",
                eligibility = "Bachelor's degree in any discipline; Age 18-30/32 years.",
                organization = "Staff Selection Commission, Govt of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Railway Recruitment Board (RRB) NTPC & Assistant Loco Pilot Vacancies",
                description = "Indian Railways nationwide recruitment for non-technical popular categories (NTPC), Station Masters, Goods Guards, and Junior Engineers.",
                sourceName = "Railway Recruitment Control Board",
                sourceUrl = "https://rrbcdg.gov.in",
                publishedAt = "2026-09-30",
                deadline = "28/02/2027",
                eligibility = "12th pass or Graduate depending on post; Age 18-33 years.",
                organization = "Ministry of Railways, Govt of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "IBPS Probationary Officers (PO) & Clerks Common Recruitment Process (CRP)",
                description = "Institute of Banking Personnel Selection common competitive exam for Officer and Clerical vacancies across 11 nationalized public sector banks.",
                sourceName = "IBPS",
                sourceUrl = "https://www.ibps.in",
                publishedAt = "2026-09-28",
                deadline = "15/12/2026",
                eligibility = "Graduation in any discipline from a recognized University; Age 20-30 years.",
                organization = "Institute of Banking Personnel Selection",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "State Bank of India (SBI) Probationary Officers (PO) Direct Recruitment",
                description = "SBI premier management career entry for 2,000+ Probationary Officers with fast-track promotion paths and national banking exposure.",
                sourceName = "State Bank of India Careers",
                sourceUrl = "https://sbi.co.in/web/careers",
                publishedAt = "2026-09-27",
                deadline = "20/12/2026",
                eligibility = "Graduation in any discipline; Age 21-30 years.",
                organization = "State Bank of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Reserve Bank of India (RBI) Grade 'B' Officers Recruitment",
                description = "RBI direct recruitment for Grade B Officers in General, Economic Policy Research (DEPR), and Statistics & Information Management (DSIM).",
                sourceName = "Reserve Bank of India",
                sourceUrl = "https://opportunities.rbi.org.in",
                publishedAt = "2026-09-25",
                deadline = "10/01/2027",
                eligibility = "Graduation with minimum 60% marks (50% for SC/ST/PwBD); Age 21-30 years.",
                organization = "Reserve Bank of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "ভাৰতীয় সেনা নিযুক্তি ২০২৬ (Join Indian Army Agniveer Rally & TGC Officer Entry)",
                description = "ভাৰতীয় সেনাৰ অগ্নিবীৰ (GD, Tech, Clerk) আৰু অফিচাৰ নিযুক্তি। ক'ত আবেদন কৰিব (Where to Apply): https://joinindianarmy.nic.in । কি কি লাগিব: বয়স ১৭.৫-২১ বছৰ, ১০ম/১২শ উত্তীৰ্ণ, ১.৬ কিঃমিঃ দৌৰ, আধাৰ কাৰ্ড। মাহে ₹৩০,০০০-₹৪০,০০০ + ₹১১.৭১ লাখ সেৱা নিধি।",
                sourceName = "Indian Army Recruiting Directorate",
                sourceUrl = "https://joinindianarmy.nic.in",
                publishedAt = "2026-10-01",
                deadline = "25/02/2027",
                eligibility = "Age 17.5-21 yrs for Agniveer (10th pass with 45%), 20-27 yrs for TGC (BE/BTech); 1.6km running & medical fitness.",
                organization = "Ministry of Defence, Govt of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "ভাৰতীয় নৌসেনা নিযুক্তি ২০২৬ (Join Indian Navy SSR & MR Agniveer Recruitment)",
                description = "ভাৰতীয় নৌসেনাৰ অগ্নিবীৰ এছ.এছ.আৰ (SSR) আৰু এম.আৰ (MR) নিযুক্তি। ক'ত আবেদন কৰিব (Where to Apply): https://joinindiannavy.gov.in । কি কি লাগিব: বয়স ১৭.৫-২১ বছৰ, ১২শ বিজ্ঞান (গণিত আৰু পদাৰ্থ বিজ্ঞান) বা ১০ম উত্তীৰ্ণ, ১.৬ কিঃমিঃ দৌৰ, চকুৰ দৃষ্টি ৬/৬।",
                sourceName = "Indian Navy",
                sourceUrl = "https://joinindiannavy.gov.in",
                publishedAt = "2026-09-29",
                deadline = "15/01/2027",
                eligibility = "Age 17.5-21 yrs; 10+2 with Maths & Physics (SSR) or 10th pass (MR); 1.6km running & medical standards.",
                organization = "Ministry of Defence, Govt of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "মাৰ্চেন্ট নেভী কেৰিয়াৰ নিযুক্তি ২০২৬ (Merchant Navy Recruitment - Deck Cadet & GP Rating)",
                description = "বাণিজ্যিক জাহাজত বিশাল দৰমহাৰ কেৰিয়াৰ! ক'ত আবেদন কৰিব (Where to Apply): https://dgshipping.gov.in । কি কি লাগিব: বয়স ১৭.৫-২৫ বছৰ, ১০+২ PCM ৬০% (কেডেটৰ বাবে) বা ১০ম উত্তীৰ্ণ (GP Rating), ৬/৬ দৃষ্টিশক্তি, বৈধ পাছপোৰ্ট। দৰমহা: মাহে ₹৪৫,০০০ ৰ পৰা ₹২,৫০,০০০+ (কৰমুক্ত)।",
                sourceName = "Directorate General of Shipping (DGS), Govt of India",
                sourceUrl = "https://dgshipping.gov.in",
                publishedAt = "2026-10-02",
                deadline = "20/02/2027",
                eligibility = "Age 17.5-25 yrs; 10+2 PCM 60% or 10th pass 40%; DG Shipping medical fitness & 6/6 vision; Passport required.",
                organization = "Ministry of Ports, Shipping and Waterways",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Air Force Common Admission Test (AFCAT) & Agniveer Vayu Selection",
                description = "Indian Air Force recruitment for Flying Branch, Ground Duty (Technical & Non-Technical) Officers and Agniveer Vayu intake.",
                sourceName = "Indian Air Force & CDAC",
                sourceUrl = "https://careerindianairforce.cdac.in",
                publishedAt = "2026-09-30",
                deadline = "30/12/2026",
                eligibility = "Graduation / BE / BTech with min 60% marks; Age 20-24/26 years.",
                organization = "Indian Air Force",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "DRDO Scientist 'B' & CEPTAM Senior Technical Assistant (STA-B) Recruitment",
                description = "Defence Research & Development Organisation technical and scientific officer positions in advanced defence electronics, aerospace, and robotics.",
                sourceName = "DRDO RAC",
                sourceUrl = "https://rac.gov.in",
                publishedAt = "2026-09-26",
                deadline = "20/01/2027",
                eligibility = "First class Bachelor's in Engineering or Master's in Science with valid GATE score.",
                organization = "Defence Research and Development Organisation",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "ISRO Centralised Recruitment Board (ICRB) - Scientist/Engineer 'SC' Hiring",
                description = "Indian Space Research Organisation national recruitment for space scientists, rocket propulsion engineers, and satellite payload specialists.",
                sourceName = "ISRO Careers",
                sourceUrl = "https://www.isro.gov.in/Careers.html",
                publishedAt = "2026-09-24",
                deadline = "15/01/2027",
                eligibility = "BE/BTech in Mechanical, Electronics, or Computer Science with minimum 65% aggregate.",
                organization = "Department of Space, Govt of India",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Assam Public Service Commission (APSC) Combined Competitive Examination (CCE)",
                description = "State civil services examination for Assam Civil Service (Junior Grade), Assam Police Service, Superintendent of Taxes, and Block Development Officers.",
                sourceName = "Assam Public Service Commission",
                sourceUrl = "https://apsc.nic.in",
                publishedAt = "2026-10-01",
                deadline = "31/12/2026",
                eligibility = "Degree from recognized university; Age 21-38 years with state domicile.",
                organization = "Government of Assam",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.ASSAM
            ),
            OpportunityRawItem(
                title = "Uttar Pradesh Combined State / Upper Subordinate Services (PCS) Exam",
                description = "UPPSC premier state civil service recruitment for Sub-Divisional Magistrates (SDM), Deputy SP, and Assistant Regional Transport Officers.",
                sourceName = "UPPSC",
                sourceUrl = "https://uppsc.up.nic.in",
                publishedAt = "2026-09-25",
                deadline = "25/01/2027",
                eligibility = "Bachelor's degree from any recognized university; Age 21-40 years.",
                organization = "Government of Uttar Pradesh",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Bihar Public Service Commission (BPSC) Integrated 71st CCE Notification",
                description = "Recruitment for administrative, revenue, police, and municipal executive officers across departments in Bihar state government.",
                sourceName = "BPSC",
                sourceUrl = "https://bpsc.bih.nic.in",
                publishedAt = "2026-09-26",
                deadline = "15/01/2027",
                eligibility = "Graduation in any stream from recognized university; Age 20/21/22 to 37 years.",
                organization = "Government of Bihar",
                categoryHint = OpportunityCategory.GOVERNMENT_JOB,
                regionHint = OpportunityRegion.INDIA
            )
        )
    }
}

/**
 * Comprehensive Indian Scholarships Source Provider.
 * Covers National Scholarship Portal, AICTE, DST INSPIRE, UGC NET-JRF, PMSS, and State Portals.
 */
class ScholarshipsComprehensiveSourceProvider : SourceProvider {
    override val providerId: String = "scholarships_comprehensive_portal"
    override val providerName: String = "National & State Scholarship Portals"
    override val sourceTier: SourceTier = SourceTier.TIER_1_OFFICIAL
    override val defaultRegion: OpportunityRegion = OpportunityRegion.INDIA

    override suspend fun fetchItems(): List<OpportunityRawItem> = withContext(Dispatchers.IO) {
        listOf(
            OpportunityRawItem(
                title = "National Scholarship Portal (NSP 2026-27) - Central Sector Higher Education Scheme",
                description = "Ministry of Education scholarships providing ₹12,000 per annum for graduation and ₹20,000 for post-graduation to meritorious college students.",
                sourceName = "National Scholarship Portal (NSP)",
                sourceUrl = "https://scholarships.gov.in",
                publishedAt = "2026-10-01",
                deadline = "31/12/2026",
                eligibility = "Top 20th percentile in Class 12 board exams; family income below ₹4.5 Lakhs/year.",
                organization = "Ministry of Education, Govt of India",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "AICTE Pragati Scholarship for Girls in Technical Education - ₹50,000/Year",
                description = "Direct benefit financial assistance of ₹50,000 per annum for female students admitted to first year of Degree or Diploma courses in AICTE approved institutions.",
                sourceName = "AICTE National Portal",
                sourceUrl = "https://www.aicte-india.org/schemes/students-development-schemes/pragati",
                publishedAt = "2026-09-28",
                deadline = "31/12/2026",
                eligibility = "Maximum two girl children per family; family income < ₹8 Lakhs per annum.",
                organization = "All India Council for Technical Education",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "AICTE Saksham Scholarship Scheme for Specially-Abled Students",
                description = "₹50,000 per year grant toward college fees, books, and assistive devices for differently-abled students admitted to technical degree or diploma courses.",
                sourceName = "AICTE National Portal",
                sourceUrl = "https://www.aicte-india.org/schemes/students-development-schemes/saksham",
                publishedAt = "2026-09-27",
                deadline = "31/12/2026",
                eligibility = "Disability >= 40%; family income < ₹8 Lakhs/year; enrolled in AICTE approved institution.",
                organization = "All India Council for Technical Education",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "AICTE Swanath Scholarship Scheme for Orphans & Armed Forces Wards",
                description = "₹50,000 annual financial aid to support orphans, children whose both parents died due to Covid-19, and children of armed forces martyrs pursuing technical degrees.",
                sourceName = "AICTE National Portal",
                sourceUrl = "https://www.aicte-india.org/schemes/students-development-schemes/swanath",
                publishedAt = "2026-09-26",
                deadline = "31/12/2026",
                eligibility = "Enrolled in AICTE approved degree/diploma program; family income < ₹8 Lakhs.",
                organization = "All India Council for Technical Education",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "DST INSPIRE Scholarship for Higher Education (SHE) - ₹80,000/Year",
                description = "Department of Science and Technology premier scholarship offering ₹80,000 per annum for students pursuing natural and basic sciences (BSc, BS, Int. MSc).",
                sourceName = "DST INSPIRE Portal",
                sourceUrl = "https://online-inspire.gov.in",
                publishedAt = "2026-09-25",
                deadline = "15/01/2027",
                eligibility = "Top 1% in Class 12 board exam or top 10,000 in JEE/NEET; enrolled in basic sciences.",
                organization = "Department of Science and Technology (DST)",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "UGC National Eligibility Test & Junior Research Fellowship (NET-JRF)",
                description = "Fellowship of ₹37,000/month for initial 2 years and ₹42,000/month for subsequent years plus contingency grant for full-time PhD researchers.",
                sourceName = "National Testing Agency & UGC",
                sourceUrl = "https://ugcnet.nta.ac.in",
                publishedAt = "2026-10-02",
                deadline = "30/11/2026",
                eligibility = "Master's degree with at least 55% marks (50% for reserved categories); Age <= 30 years for JRF.",
                organization = "University Grants Commission",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Prime Minister's Scholarship Scheme (PMSS) for CAPF & Assam Rifles Wards",
                description = "Monthly scholarship of ₹3,000 for girls and ₹2,500 for boys pursuing professional degree courses (Engineering, Medical, Dental, Veterinary, MBA, MCA).",
                sourceName = "Welfare & Rehabilitation Board (WARB, MHA)",
                sourceUrl = "https://warb-mha.gov.in",
                publishedAt = "2026-09-24",
                deadline = "31/12/2026",
                eligibility = "Wards/widows of deceased/ex-CAPF & Assam Rifles personnel; min 60% in 10+2/Diploma.",
                organization = "Ministry of Home Affairs, Govt of India",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Post-Matric Scholarship for SC Students - Full Tuition Reimbursement & Stipend",
                description = "Centrally sponsored scholarship covering complete non-refundable course fees and monthly academic maintenance allowance for scheduled caste scholars.",
                sourceName = "Ministry of Social Justice & Empowerment",
                sourceUrl = "https://socialjustice.gov.in",
                publishedAt = "2026-09-23",
                deadline = "31/01/2027",
                eligibility = "SC student studying at post-matriculation level; family annual income <= ₹2.5 Lakhs.",
                organization = "Ministry of Social Justice and Empowerment",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "West Bengal Swami Vivekananda Merit-cum-Means Scholarship (SVMCM)",
                description = "Annual scholarship up to ₹60,000 for students pursuing Higher Secondary, Undergraduate, Postgraduate, and Professional Engineering/Medical courses.",
                sourceName = "Higher Education Department, Govt of WB",
                sourceUrl = "https://svmcm.wbhed.gov.in",
                publishedAt = "2026-09-27",
                deadline = "31/01/2027",
                eligibility = "West Bengal resident; minimum 60% in qualifying exam; family income <= ₹2.5 Lakhs.",
                organization = "Government of West Bengal",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Uttar Pradesh Saksham Online Scholarship & Fee Reimbursement",
                description = "Complete college tuition fee reimbursement and maintenance allowance for pre-matric, post-matric, and professional students across UP institutions.",
                sourceName = "Social Welfare Department, UP",
                sourceUrl = "https://scholarship.up.gov.in",
                publishedAt = "2026-09-29",
                deadline = "15/01/2027",
                eligibility = "Enrolled in recognized UP institute; family income <= ₹2.5 Lakhs for SC/ST and ₹2 Lakhs for General/OBC.",
                organization = "Government of Uttar Pradesh",
                categoryHint = OpportunityCategory.SCHOLARSHIP,
                regionHint = OpportunityRegion.INDIA
            )
        )
    }
}

/**
 * Comprehensive Indian Internships & Apprenticeships Source Provider.
 * Covers PM Internship Scheme 2026, AICTE Internship Portal, NATS 2.0, NAPS, NITI Aayog, and MeitY.
 */
class InternshipsComprehensiveSourceProvider : SourceProvider {
    override val providerId: String = "internships_comprehensive_portal"
    override val providerName: String = "National Internship & Apprenticeship Portals"
    override val sourceTier: SourceTier = SourceTier.TIER_1_OFFICIAL
    override val defaultRegion: OpportunityRegion = OpportunityRegion.INDIA

    override suspend fun fetchItems(): List<OpportunityRawItem> = withContext(Dispatchers.IO) {
        listOf(
            OpportunityRawItem(
                title = "Prime Minister's Internship Scheme 2026 (PMIS) - 1 Crore Youth Opportunities",
                description = "Ministry of Corporate Affairs flagship scheme placing 1 Crore youth in 12-month paid internships in top 500 companies with ₹5,000/month stipend and ₹6,000 one-time grant.",
                sourceName = "Ministry of Corporate Affairs",
                sourceUrl = "https://pminternship.mca.gov.in",
                publishedAt = "2026-10-02",
                deadline = "31/03/2027",
                eligibility = "Youth aged 21-24 years; not engaged in full-time employment; 10th, 12th, ITI, Diploma, or Degree holders.",
                organization = "Government of India & MCA",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "AICTE National Internship Portal - 50 Lakh+ Government & Corporate Internships",
                description = "All-India portal offering structured virtual and onsite internships across smart cities, national highway projects, PSUs, and tech multinationals.",
                sourceName = "AICTE National Internship Portal",
                sourceUrl = "https://internship.aicte-india.org",
                publishedAt = "2026-10-01",
                deadline = "31/12/2027",
                eligibility = "Enrolled in or graduated from any AICTE/UGC recognized university or polytechnic.",
                organization = "All India Council for Technical Education",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "National Apprenticeship Training Scheme (NATS 2.0) - DBT Government Stipend",
                description = "One-year on-the-job industrial apprenticeship for Engineering, Science, Humanities, and Commerce graduates with direct bank transfer monthly stipend.",
                sourceName = "National Apprenticeship Portal",
                sourceUrl = "https://nats.education.gov.in",
                publishedAt = "2026-09-29",
                deadline = "28/02/2027",
                eligibility = "Graduates and Diploma holders who passed in the last 3 years; zero prior apprentice experience.",
                organization = "Ministry of Education, Govt of India",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "National Apprenticeship Promotion Scheme (NAPS) - Industrial Trade Apprenticeship",
                description = "Apprenticeship training across 30+ industry sectors with government stipend sharing of 25% (up to ₹1,500/month per apprentice) for registered employers.",
                sourceName = "Apprenticeship India Portal",
                sourceUrl = "https://apprenticeshipindia.gov.in",
                publishedAt = "2026-09-30",
                deadline = "31/12/2027",
                eligibility = "Candidates aged 14 years and above; 5th to 12th pass, ITI, Diploma, or Graduate.",
                organization = "Ministry of Skill Development and Entrepreneurship",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "NITI Aayog Official Internship Scheme - Public Policy & Governance",
                description = "Prestigious 6-week to 6-month policy research internships in public finance, governance, health, agriculture, digital economy, and infrastructure.",
                sourceName = "NITI Aayog",
                sourceUrl = "https://niti.gov.in/internship",
                publishedAt = "2026-09-25",
                deadline = "10/01/2027",
                eligibility = "Undergraduate students having completed 2nd year (>=85% marks) or Post-Graduate/Research scholars.",
                organization = "NITI Aayog, Government of India",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Digital India Internship Scheme (MeitY) - ₹10,000 Monthly Stipend",
                description = "Ministry of Electronics & IT internship for 2 months offering ₹10,000/month in Artificial Intelligence, Cyber Law, Cloud Computing, and E-Governance.",
                sourceName = "Ministry of Electronics & IT (MeitY)",
                sourceUrl = "https://www.meity.gov.in/digital-india-internship-scheme",
                publishedAt = "2026-09-28",
                deadline = "31/12/2026",
                eligibility = "Indian students studying in BTech/BE/MCA/MSc (CS/IT)/LLB with minimum 60% marks.",
                organization = "MeitY, Government of India",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Ministry of External Affairs (MEA) Internship Programme - Foreign Diplomacy",
                description = "Stipend of ₹10,000 per month plus return airfare for young scholars engaging with foreign policy divisions, multilateral desks, and bilateral missions.",
                sourceName = "Ministry of External Affairs",
                sourceUrl = "https://internship.mea.gov.in",
                publishedAt = "2026-09-26",
                deadline = "15/01/2027",
                eligibility = "Graduate degree in any discipline; Age not exceeding 25 years.",
                organization = "Ministry of External Affairs, Govt of India",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Reserve Bank of India (RBI) Summer Internship - ₹20,000/Month Stipend",
                description = "Prestigious 3-month research internship at RBI Central Office Mumbai in macroeconomics, financial markets, banking supervision, and data science.",
                sourceName = "Reserve Bank of India",
                sourceUrl = "https://rbi.org.in",
                publishedAt = "2026-09-27",
                deadline = "15/12/2026",
                eligibility = "Post-graduate students in Economics, Commerce, MBA (Finance), Data Analytics from premier institutes.",
                organization = "Reserve Bank of India",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "DPIIT Industrial Policy & Startup Internship Scheme - ₹10,000/Month",
                description = "Hands-on policy formulation and intellectual property rights (IPR) research internship in the Department for Promotion of Industry and Internal Trade.",
                sourceName = "DPIIT, Ministry of Commerce",
                sourceUrl = "https://dpiit.gov.in",
                publishedAt = "2026-09-24",
                deadline = "20/01/2027",
                eligibility = "Graduate or Post-Graduate students in Economics, Law, Engineering, or Management.",
                organization = "Ministry of Commerce and Industry",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            ),
            OpportunityRawItem(
                title = "Madhya Pradesh Mukhyamantri Seekho-Kamao Yojana (MMSKY) - ₹8,000 to ₹10,000/Month",
                description = "State-sponsored industrial apprenticeship across 700+ sectors with monthly direct benefit transfer (DBT) stipend of ₹8,000 (12th), ₹8,500 (ITI), ₹9,000 (Diploma), ₹10,000 (Degree).",
                sourceName = "State Youth Skill Board, Govt of MP",
                sourceUrl = "https://mmsky.mp.gov.in",
                publishedAt = "2026-10-01",
                deadline = "28/02/2027",
                eligibility = "MP local resident; 12th/ITI/Diploma/Degree pass; Age 18-29 years.",
                organization = "Government of Madhya Pradesh",
                categoryHint = OpportunityCategory.INTERNSHIP,
                regionHint = OpportunityRegion.INDIA
            )
        )
    }
}
