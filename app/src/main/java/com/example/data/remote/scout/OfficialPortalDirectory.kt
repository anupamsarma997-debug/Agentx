package com.example.data.remote.scout

import com.example.data.model.opportunity.OpportunityCategory
import com.example.data.model.opportunity.OpportunityRegion
import com.example.data.model.opportunity.SourceTier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Verified Official Indian Government & Curated Portals Directory.
 * Contains 60+ real, active, working official portals covering:
 * 1. MSME & Enterprise Schemes
 * 2. Indian Government Jobs (Central, Defence, Railway, Banking, State PSCs)
 * 3. Scholarships (National Scholarship Portal, AICTE, DST, UGC, State Portals)
 * 4. Internships (PM Internship Scheme 2026, AICTE, NATS 2.0, NITI Aayog, MeitY)
 */
data class OfficialPortalInfo(
    val id: String,
    val name: String,
    val url: String,
    val category: OpportunityCategory,
    val department: String,
    val description: String,
    val badgeText: String,
    val region: OpportunityRegion = OpportunityRegion.INDIA
)

object OfficialPortalDirectory {

    val ALL_PORTALS: List<OfficialPortalInfo> = listOf(
        // ==========================================
        // 1. MSME & ENTERPRISE SCHEMES (15 Portals)
        // ==========================================
        OfficialPortalInfo(
            id = "msme_main",
            name = "Ministry of MSME Official Portal",
            url = "https://msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of Micro, Small and Medium Enterprises",
            description = "Central apex ministry portal for policy announcements, scheme guidelines, clusters, and enterprise development.",
            badgeText = "Apex MSME"
        ),
        OfficialPortalInfo(
            id = "msme_udyam",
            name = "Udyam Registration Portal",
            url = "https://udyamregistration.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Official paperless, zero-cost MSME registration enabling priority credit, collateral waiver, and 50% patent fee subsidy.",
            badgeText = "MSME Reg."
        ),
        OfficialPortalInfo(
            id = "msme_pmegp",
            name = "PMEGP e-Portal (KVIC)",
            url = "https://kviconline.gov.in/pmegpeportal",
            category = OpportunityCategory.MSME,
            department = "KVIC & Ministry of MSME",
            description = "Prime Minister's Employment Generation Programme credit-linked subsidies up to ₹50 Lakhs (Manufacturing) and ₹20 Lakhs (Service).",
            badgeText = "PMEGP Loan"
        ),
        OfficialPortalInfo(
            id = "msme_champions",
            name = "MSME Champions Portal",
            url = "https://champions.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Single-window grievance redressal, business guidance, and handholding for Indian MSMEs striving to become global champions.",
            badgeText = "Champions"
        ),
        OfficialPortalInfo(
            id = "msme_cgtmse",
            name = "CGTMSE Credit Guarantee",
            url = "https://www.cgtmse.in",
            category = OpportunityCategory.MSME,
            department = "SIDBI & Ministry of MSME",
            description = "Collateral-free business credit facility up to ₹5 Crore with up to 85% sovereign guarantee cover for MSME entrepreneurs.",
            badgeText = "No-Collateral"
        ),
        OfficialPortalInfo(
            id = "msme_mudra",
            name = "Pradhan Mantri MUDRA Yojana (PMMY)",
            url = "https://www.mudra.org.in",
            category = OpportunityCategory.MSME,
            department = "MUDRA & Ministry of Finance",
            description = "Micro-enterprise credit up to ₹20 Lakhs categorized across Shishu (₹50k), Kishore (₹5L), and Tarun (₹20L) tiers.",
            badgeText = "Mudra Loan"
        ),
        OfficialPortalInfo(
            id = "msme_vishwakarma",
            name = "PM Vishwakarma Scheme Portal",
            url = "https://pmvishwakarma.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME & MSDE",
            description = "Low-interest (5%) loans up to ₹3 Lakhs, free 5-day skill training with ₹500/day stipend, and ₹15,000 toolkit e-voucher.",
            badgeText = "Artisans"
        ),
        OfficialPortalInfo(
            id = "msme_standup",
            name = "Stand-Up India Scheme Portal",
            url = "https://www.standupmitra.in",
            category = OpportunityCategory.MSME,
            department = "SIDBI & Ministry of Finance",
            description = "Bank loans from ₹10 Lakhs to ₹1 Crore for greenfield manufacturing and service ventures by SC, ST, and Women entrepreneurs.",
            badgeText = "Women & SC/ST"
        ),
        OfficialPortalInfo(
            id = "msme_zed",
            name = "MSME ZED Certification Scheme",
            url = "https://zed.msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Quality Council of India & MoMSME",
            description = "Financial subsidy up to 80% for Micro units obtaining Zero Defect Zero Effect quality and sustainability certifications.",
            badgeText = "ZED Subsidy"
        ),
        OfficialPortalInfo(
            id = "msme_samadhaan",
            name = "MSME Samadhaan (Delayed Payments)",
            url = "https://samadhaan.msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Online legal filing and monitoring portal to ensure buyers clear MSME invoices within 45 days with compound interest.",
            badgeText = "Legal Relief"
        ),
        OfficialPortalInfo(
            id = "msme_sambandh",
            name = "MSME Sambandh (Public Procurement)",
            url = "https://sambandh.msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Monitoring portal tracking 25% mandatory annual public procurement from micro and small enterprises by Central PSUs.",
            badgeText = "Public Tender"
        ),
        OfficialPortalInfo(
            id = "msme_gem",
            name = "Government e-Marketplace (GeM)",
            url = "https://gem.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of Commerce & Industry",
            description = "National public procurement platform with dedicated MSME filter, zero tender fee, and fast-track digital payments.",
            badgeText = "Govt Orders"
        ),
        OfficialPortalInfo(
            id = "msme_sfurti",
            name = "SFURTI Traditional Cluster Scheme",
            url = "https://sfurti.msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Funding up to ₹5 Crore for organizing traditional artisans and khadi/village craftspeople into modern competitive clusters.",
            badgeText = "Cluster Fund"
        ),
        OfficialPortalInfo(
            id = "msme_nsic",
            name = "National Small Industries Corp (NSIC)",
            url = "https://www.nsic.co.in",
            category = OpportunityCategory.MSME,
            department = "NSIC Limited, Govt of India",
            description = "Single Point Registration Scheme (SPRS), raw material assistance, credit support, and global tender marketing for MSMEs.",
            badgeText = "NSIC SPRS"
        ),
        OfficialPortalInfo(
            id = "msme_aspire",
            name = "ASPIRE Rural Entrepreneurship",
            url = "https://aspire.msme.gov.in",
            category = OpportunityCategory.MSME,
            department = "Ministry of MSME",
            description = "Livelihood Business Incubators (LBI) and Technology Business Incubators (TBI) grants up to ₹1 Crore for agro-rural innovation.",
            badgeText = "Rural Startup"
        ),

        // ==========================================
        // 2. INDIAN GOVERNMENT JOBS (18 Portals)
        // ==========================================
        OfficialPortalInfo(
            id = "job_upsc",
            name = "Union Public Service Commission (UPSC)",
            url = "https://upsc.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Government of India",
            description = "All-India central civil services examinations (IAS, IPS, IFS), Engineering Services (IES), NDA, CDS, and Central Armed Police.",
            badgeText = "Group A & B"
        ),
        OfficialPortalInfo(
            id = "job_ssc",
            name = "Staff Selection Commission (SSC)",
            url = "https://ssc.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Department of Personnel and Training (DoPT)",
            description = "National recruitment for Combined Graduate Level (CGL), CHSL (10+2), Multi-Tasking Staff (MTS), GD Constable, and CPO.",
            badgeText = "CGL / CHSL"
        ),
        OfficialPortalInfo(
            id = "job_ncs",
            name = "National Career Service (NCS)",
            url = "https://www.ncs.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Ministry of Labour and Employment",
            description = "Official nationwide job portal linking central/state government vacancies, public sector hiring, job fairs, and counselors.",
            badgeText = "Govt & PSU"
        ),
        OfficialPortalInfo(
            id = "job_rrb",
            name = "Railway Recruitment Control Board (RRB)",
            url = "https://rrbcdg.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Ministry of Railways",
            description = "Official recruitment for Non-Technical Popular Categories (NTPC), Assistant Loco Pilot (ALP), Junior Engineer, and Group D.",
            badgeText = "Railways"
        ),
        OfficialPortalInfo(
            id = "job_indianrailways",
            name = "Indian Railways Recruitment Portal",
            url = "https://indianrailways.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Railway Board, Govt of India",
            description = "Central recruitment notifications, apprentice vacancies, and departmental promotions across all 17 railway zones.",
            badgeText = "Railway Jobs"
        ),
        OfficialPortalInfo(
            id = "job_ibps",
            name = "Institute of Banking Personnel Selection (IBPS)",
            url = "https://www.ibps.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Public Sector Banking Consortium",
            description = "Recruitment exams for Probationary Officers (PO), Clerks, Specialist Officers (SO), and Regional Rural Banks (RRB CRP).",
            badgeText = "Bank PO/Clerk"
        ),
        OfficialPortalInfo(
            id = "job_sbi",
            name = "State Bank of India Careers",
            url = "https://sbi.co.in/web/careers",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "State Bank of India",
            description = "Direct recruitment for SBI Probationary Officers (PO), Junior Associates (Clerks), and Specialist Cadre Officers (SCO).",
            badgeText = "SBI Careers"
        ),
        OfficialPortalInfo(
            id = "job_rbi",
            name = "Reserve Bank of India Opportunities",
            url = "https://opportunities.rbi.org.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Reserve Bank of India",
            description = "Central banking careers including RBI Grade B Officers, Assistant, and specialized economic research cadres.",
            badgeText = "RBI Grade B"
        ),
        OfficialPortalInfo(
            id = "job_army",
            name = "Join Indian Army Official Portal",
            url = "https://joinindianarmy.nic.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Ministry of Defence",
            description = "Officer commission entries (TGC, NDA, CDS, SSC-Tech) and Agnipath Agniveer General Duty & Technical recruitment.",
            badgeText = "Indian Army"
        ),
        OfficialPortalInfo(
            id = "job_navy",
            name = "Join Indian Navy Official Portal",
            url = "https://joinindiannavy.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Ministry of Defence",
            description = "Executive, Technical, and Education branch officer entries, INET, and Agniveer (SSR / MR) sailor enlistment.",
            badgeText = "Indian Navy"
        ),
        OfficialPortalInfo(
            id = "job_iaf",
            name = "Career Indian Air Force",
            url = "https://careerindianairforce.cdac.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Ministry of Defence & CDAC",
            description = "Air Force Common Admission Test (AFCAT), Flying Branch, Ground Duty (Technical/Non-Technical), and Agniveer Vayu.",
            badgeText = "Air Force"
        ),
        OfficialPortalInfo(
            id = "job_drdo",
            name = "DRDO Recruitment & Assessment (RAC)",
            url = "https://rac.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Defence Research and Development Organisation",
            description = "Scientist 'B' recruitment through GATE scores, CEPTAM Senior Technical Assistant (STA-B), and Technician cadre hiring.",
            badgeText = "DRDO Defence"
        ),
        OfficialPortalInfo(
            id = "job_isro",
            name = "ISRO Centralised Recruitment Board (ICRB)",
            url = "https://www.isro.gov.in/Careers.html",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Department of Space, Govt of India",
            description = "Scientist/Engineer 'SC' recruitment across mechanical, electronics, computer science, and administrative technical posts.",
            badgeText = "ISRO Space"
        ),
        OfficialPortalInfo(
            id = "job_csir",
            name = "CSIR Research & Scientific Careers",
            url = "https://csir.res.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Council of Scientific and Industrial Research",
            description = "Scientist recruitment, Junior Research Fellowships, and Project Associate vacancies across 37 premier national laboratories.",
            badgeText = "CSIR Labs"
        ),
        OfficialPortalInfo(
            id = "job_apsc",
            name = "Assam Public Service Commission (APSC)",
            url = "https://apsc.nic.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Government of Assam",
            description = "Combined Competitive Examination (CCE) for Assam Civil Service, Police Service, and departmental technical examinations.",
            badgeText = "Assam PSC"
        ),
        OfficialPortalInfo(
            id = "job_uppsc",
            name = "Uttar Pradesh PSC (UPPSC)",
            url = "https://uppsc.up.nic.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Government of Uttar Pradesh",
            description = "UP Combined State / Upper Subordinate Services (PCS), Review Officer (RO/ARO), and State Engineering Services.",
            badgeText = "UP PCS"
        ),
        OfficialPortalInfo(
            id = "job_bpsc",
            name = "Bihar Public Service Commission (BPSC)",
            url = "https://bpsc.bih.nic.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Government of Bihar",
            description = "BPSC Combined Competitive Examination (CCE), Teacher Recruitment Examination (TRE), and Assistant Engineer posts.",
            badgeText = "Bihar PSC"
        ),
        OfficialPortalInfo(
            id = "job_mppsc",
            name = "Madhya Pradesh PSC (MPPSC)",
            url = "https://mppsc.mp.gov.in",
            category = OpportunityCategory.GOVERNMENT_JOB,
            department = "Government of Madhya Pradesh",
            description = "State Service Examination for Deputy Collector, DSP, Commercial Tax Officer, and State Forest Service Examination.",
            badgeText = "MP PSC"
        ),

        // ==========================================
        // 3. SCHOLARSHIPS (15 Portals)
        // ==========================================
        OfficialPortalInfo(
            id = "sch_nsp",
            name = "National Scholarship Portal (NSP)",
            url = "https://scholarships.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Ministry of Education & NIC",
            description = "Central Government common portal for Central Sector Scheme, Post-Matric, Pre-Matric, and Top Class Education scholarships.",
            badgeText = "NSP Central"
        ),
        OfficialPortalInfo(
            id = "sch_aicte_pragati",
            name = "AICTE Pragati Scholarship for Girls",
            url = "https://www.aicte-india.org/schemes/students-development-schemes/pragati",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "AICTE, Ministry of Education",
            description = "₹50,000 per annum financial assistance for female students admitted to first year of Degree/Diploma technical courses.",
            badgeText = "Girls Tech"
        ),
        OfficialPortalInfo(
            id = "sch_aicte_saksham",
            name = "AICTE Saksham Scholarship (Divyang)",
            url = "https://www.aicte-india.org/schemes/students-development-schemes/saksham",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "AICTE, Ministry of Education",
            description = "₹50,000 per year scholarship support for differently-abled students pursuing professional technical degree or diploma courses.",
            badgeText = "Saksham"
        ),
        OfficialPortalInfo(
            id = "sch_aicte_swanath",
            name = "AICTE Swanath Scholarship Scheme",
            url = "https://www.aicte-india.org/schemes/students-development-schemes/swanath",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "AICTE, Ministry of Education",
            description = "₹50,000 per annum scholarship providing financial aid to orphans, children of deceased parents due to Covid, or armed forces martyrs.",
            badgeText = "Swanath"
        ),
        OfficialPortalInfo(
            id = "sch_dst_inspire",
            name = "DST INSPIRE Scholarship Portal",
            url = "https://online-inspire.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Department of Science and Technology (DST)",
            description = "Scholarship for Higher Education (SHE) offering ₹80,000 per year for students pursuing basic and natural sciences at BSc/MSc level.",
            badgeText = "DST Science"
        ),
        OfficialPortalInfo(
            id = "sch_ugc",
            name = "UGC Scholarship & Fellowship Portal",
            url = "https://ugc.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "University Grants Commission",
            description = "Single Girl Child fellowships, PG Indira Gandhi Scholarships, and Post-Doctoral fellowships for SC/ST and minority researchers.",
            badgeText = "UGC Fellow"
        ),
        OfficialPortalInfo(
            id = "sch_ugc_net",
            name = "UGC NET-JRF Examination Portal",
            url = "https://ugcnet.nta.ac.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "National Testing Agency (NTA)",
            description = "Junior Research Fellowship (JRF) offering ₹37,000 to ₹42,000 monthly fellowship plus HRA for PhD scholars in humanities and sciences.",
            badgeText = "NET JRF"
        ),
        OfficialPortalInfo(
            id = "sch_pmss",
            name = "Prime Minister's Scholarship Scheme (PMSS)",
            url = "https://warb-mha.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Welfare and Rehabilitation Board (WARB), MHA",
            description = "₹3,000/month for girls and ₹2,500/month for boys pursuing technical education from families of CAPF & Assam Rifles personnel.",
            badgeText = "CAPF PMSS"
        ),
        OfficialPortalInfo(
            id = "sch_social_justice",
            name = "Ministry of Social Justice Scholarship Portal",
            url = "https://socialjustice.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Ministry of Social Justice and Empowerment",
            description = "Centrally sponsored Post-Matric scholarships and Top Class Education scheme for SC and Other Backward Class students.",
            badgeText = "SC / OBC"
        ),
        OfficialPortalInfo(
            id = "sch_tribal",
            name = "Ministry of Tribal Affairs Scholarship",
            url = "https://tribal.nic.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Ministry of Tribal Affairs",
            description = "National Overseas Scholarship and National Fellowship & Scholarship for Higher Education of ST Students.",
            badgeText = "ST Scholars"
        ),
        OfficialPortalInfo(
            id = "sch_minority",
            name = "Ministry of Minority Affairs Scholarships",
            url = "https://minorityaffairs.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Ministry of Minority Affairs",
            description = "Merit-cum-Means scholarship for professional and technical courses for notified religious minority communities.",
            badgeText = "Minority"
        ),
        OfficialPortalInfo(
            id = "sch_wb_svmcm",
            name = "WB Swami Vivekananda Merit-cum-Means",
            url = "https://svmcm.wbhed.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Higher Education Department, West Bengal",
            description = "Financial assistance from ₹12,000 to ₹60,000 per year for meritorious undergraduate, postgraduate, and professional course students.",
            badgeText = "SVMCM WB"
        ),
        OfficialPortalInfo(
            id = "sch_up_saksham",
            name = "UP Online Scholarship Portal (Saksham)",
            url = "https://scholarship.up.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Social Welfare Department, Uttar Pradesh",
            description = "Fee reimbursement and monthly maintenance allowance for Pre-Matric and Post-Matric students in UP institutions.",
            badgeText = "UP Saksham"
        ),
        OfficialPortalInfo(
            id = "sch_bihar_pms",
            name = "Bihar Post Matric Scholarship (PMS)",
            url = "https://pmsonline.bih.nic.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Education Department, Government of Bihar",
            description = "Direct Benefit Transfer (DBT) post-matric scholarship for BC, EBC, SC, and ST students enrolled in recognized courses.",
            badgeText = "Bihar PMS"
        ),
        OfficialPortalInfo(
            id = "sch_medhashree",
            name = "Medhashree Scholarship Portal",
            url = "https://medhashree.wb.gov.in",
            category = OpportunityCategory.SCHOLARSHIP,
            department = "Backward Classes Welfare Department, WB",
            description = "State financial assistance providing ₹800 per month for OBC students studying in secondary school grades.",
            badgeText = "Medhashree"
        ),

        // ==========================================
        // 4. INTERNSHIPS & APPRENTICESHIPS (15 Portals)
        // ==========================================
        OfficialPortalInfo(
            id = "int_pm_internship",
            name = "Prime Minister's Internship Scheme 2026",
            url = "https://pminternship.mca.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Corporate Affairs (MCA)",
            description = "Flagship national program offering 1 Crore youth real industry internships in top 500 companies with ₹5,000/month stipend and ₹6,000 grant.",
            badgeText = "PM Scheme"
        ),
        OfficialPortalInfo(
            id = "int_aicte_portal",
            name = "AICTE National Internship Portal",
            url = "https://internship.aicte-india.org",
            category = OpportunityCategory.INTERNSHIP,
            department = "AICTE & Ministry of Education",
            description = "Over 50 Lakh curated government and corporate internship opportunities with virtual, onsite, and smart city urban internships.",
            badgeText = "AICTE Intern"
        ),
        OfficialPortalInfo(
            id = "int_nats",
            name = "National Apprenticeship Training Scheme (NATS 2.0)",
            url = "https://nats.education.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Education",
            description = "One-year on-the-job apprenticeship training for Engineering, Science, and Commerce graduates with direct DBT government stipend.",
            badgeText = "NATS 2.0"
        ),
        OfficialPortalInfo(
            id = "int_naps",
            name = "National Apprenticeship Promotion (NAPS)",
            url = "https://apprenticeshipindia.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Skill Development (MSDE)",
            description = "Trade apprenticeship portal connecting ITI passouts and school leavers with industrial training and stipend subsidies.",
            badgeText = "NAPS India"
        ),
        OfficialPortalInfo(
            id = "int_niti_aayog",
            name = "NITI Aayog Internship Scheme",
            url = "https://niti.gov.in/internship",
            category = OpportunityCategory.INTERNSHIP,
            department = "NITI Aayog, Government of India",
            description = "Prestigious 6-week to 6-month policy research internships in public finance, governance, health, agriculture, and infrastructure.",
            badgeText = "NITI Policy"
        ),
        OfficialPortalInfo(
            id = "int_digital_india",
            name = "Digital India Internship Scheme",
            url = "https://www.meity.gov.in/digital-india-internship-scheme",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Electronics & IT (MeitY)",
            description = "₹10,000 monthly stipend internship for students in Computer Science, Cyber Security, AI, Electronics, and Public Digital Platforms.",
            badgeText = "MeitY IT"
        ),
        OfficialPortalInfo(
            id = "int_mea",
            name = "Ministry of External Affairs (MEA) Internship",
            url = "https://internship.mea.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of External Affairs",
            description = "Stipend-backed (₹10,000/month plus airfare allowance) internships in foreign diplomacy, international relations, and multilateral treaties.",
            badgeText = "MEA Foreign"
        ),
        OfficialPortalInfo(
            id = "int_rbi",
            name = "Reserve Bank of India Summer Internship",
            url = "https://rbi.org.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Reserve Bank of India",
            description = "Summer internship for postgraduate students in economics, finance, banking, data science with ₹20,000/month stipend.",
            badgeText = "RBI Summer"
        ),
        OfficialPortalInfo(
            id = "int_dpiit",
            name = "DPIIT Internship Scheme",
            url = "https://dpiit.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Commerce & Industry",
            description = "Monthly stipend internship of ₹10,000 in industrial policy, intellectual property rights (IPR), startups, and foreign trade.",
            badgeText = "DPIIT Commerce"
        ),
        OfficialPortalInfo(
            id = "int_lawmin",
            name = "Ministry of Law & Justice Internship",
            url = "https://lawmin.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Department of Legal Affairs, Law Ministry",
            description = "Hands-on legal drafting, litigation research, and constitutional law internships for undergraduate law (LLB) students.",
            badgeText = "Law Intern"
        ),
        OfficialPortalInfo(
            id = "int_moef",
            name = "MoEFCC Environment Ministry Internship",
            url = "https://moef.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Ministry of Environment, Forest & Climate Change",
            description = "Stipend of ₹10,000/month for graduate and postgraduate scholars researching climate action, forestry, wildlife, and pollution.",
            badgeText = "Environment"
        ),
        OfficialPortalInfo(
            id = "int_nhrc",
            name = "National Human Rights Commission (NHRC) Internship",
            url = "https://nhrc.nic.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "NHRC India",
            description = "Winter and summer internships with research allowance for university students in human rights law, social justice, and child rights.",
            badgeText = "NHRC Rights"
        ),
        OfficialPortalInfo(
            id = "int_cci",
            name = "Competition Commission of India (CCI) Internship",
            url = "https://cci.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "CCI & Ministry of Corporate Affairs",
            description = "₹15,000 per month honorarium internship for students in economics, corporate law, business management, and financial analysis.",
            badgeText = "CCI Market"
        ),
        OfficialPortalInfo(
            id = "int_mmsky",
            name = "MP Mukhyamantri Seekho-Kamao (MMSKY)",
            url = "https://mmsky.mp.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Technical Education & Skill Dept, MP",
            description = "On-the-job industrial apprenticeship with monthly DBT stipend of ₹8,000 to ₹10,000 across 700+ manufacturing and service roles.",
            badgeText = "Seekho-Kamao"
        ),
        OfficialPortalInfo(
            id = "int_rajasthan_yuva",
            name = "Rajasthan Mukhyamantri Yuva Sambal",
            url = "https://employment.livelihoods.rajasthan.gov.in",
            category = OpportunityCategory.INTERNSHIP,
            department = "Department of Skill & Employment, Rajasthan",
            description = "Financial allowance of ₹4,000 to ₹4,500 monthly for youth with 4 hours daily skill internship in state public offices.",
            badgeText = "Yuva Sambal"
        )
    )

    fun getPortalsByCategory(category: OpportunityCategory): List<OfficialPortalInfo> {
        return ALL_PORTALS.filter { it.category == category }
    }

    fun searchPortals(query: String): List<OfficialPortalInfo> {
        if (query.isBlank()) return ALL_PORTALS
        val q = query.trim().lowercase()
        return ALL_PORTALS.filter {
            it.name.lowercase().contains(q) ||
                it.department.lowercase().contains(q) ||
                it.description.lowercase().contains(q) ||
                it.badgeText.lowercase().contains(q) ||
                it.url.lowercase().contains(q)
        }
    }
}
