package org.zerock.nextenter.ai.resume;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class TestDataController {

    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/create-ai-llm-resume")
    public Map<String, Object> createAiLlmResume() {
        try {
            String sql = "UPDATE resume SET " +
                    "title = ?, " +
                    "job_category = ?, " +
                    "resume_name = ?, " +
                    "resume_email = ?, " +
                    "resume_phone = ?, " +
                    "skills = ?, " +
                    "educations = ?, " +
                    "careers = ?, " +
                    "experiences = ?, " +
                    "certificates = ?, " +
                    "extracted_text = ?, " +
                    "status = ?, " +
                    "updated_at = NOW() " +
                    "WHERE resume_id = 6 AND user_id = 1";

            String skills = "[\"Python\", \"PyTorch\", \"TensorFlow\", \"Hugging Face Transformers\", \"LangChain\", \"OpenAI API\", \"Gemini API\", \"RAG\", \"Vector DB (Pinecone, Weaviate)\", \"FastAPI\", \"Docker\", \"Kubernetes\", \"AWS SageMaker\", \"MLOps\", \"Fine-tuning\", \"Prompt Engineering\"]";

            String educations = "[{\"school\": \"KAIST\", \"major\": \"인공지능학과\", \"degree\": \"석사\", \"period\": \"2020.03 ~ 2022.02\", \"status\": \"졸업\"}, {\"school\": \"서울대학교\", \"major\": \"컴퓨터공학과\", \"degree\": \"학사\", \"period\": \"2016.03 ~ 2020.02\", \"status\": \"졸업\"}]";

            String careers = "[{\"company\": \"네이버 클로바\", \"role\": \"Senior AI/LLM Engineer\", \"period\": \"2022.03 ~ 현재\", \"key_tasks\": [\"HyperCLOVA X 모델 파인튜닝 및 최적화\", \"RAG 기반 챗봇 시스템 아키텍처 설계 및 구현\", \"Vector DB 성능 최적화 (응답속도 40% 개선)\", \"LLM 프롬프트 엔지니어링 및 Few-shot Learning 적용\", \"MLOps 파이프라인 구축 (모델 배포 자동화)\"], \"experience_years\": 2.8}, {\"company\": \"카카오브레인\", \"role\": \"AI Research Engineer (인턴)\", \"period\": \"2021.06 ~ 2021.12\", \"key_tasks\": [\"KoGPT 모델 경량화 연구 (모델 크기 30% 감소)\", \"Transformer 아키텍처 최적화 실험\", \"한국어 NLP 데이터셋 구축 및 전처리\"], \"experience_years\": 0.5}]";

            String experiences = "[{\"title\": \"기업용 AI 챗봇 플랫폼 개발\", \"period\": \"2023.01 ~ 2024.12\", \"description\": \"LangChain + GPT-4 기반 엔터프라이즈 챗봇 시스템 구축. RAG 파이프라인 설계 및 Vector DB 최적화. 월간 활성 사용자 10만명 달성.\"}, {\"title\": \"오픈소스 LLM 파인튜닝 프레임워크 개발\", \"period\": \"2023.06 ~ 2024.03\", \"description\": \"LoRA, QLoRA 기반 효율적인 파인튜닝 도구 개발. GitHub Star 2.5k+ 획득. PyPI 다운로드 50만회 이상.\"}, {\"title\": \"멀티모달 AI 모델 연구\", \"period\": \"2022.09 ~ 2023.05\", \"description\": \"CLIP, BLIP 기반 이미지-텍스트 멀티모달 모델 연구. 한국어 데이터셋 구축 및 모델 성능 15% 향상.\"}]";

            String certificates = "[{\"title\": \"AWS Certified Machine Learning - Specialty\", \"date\": \"2023.08\", \"issuer\": \"Amazon Web Services\"}, {\"title\": \"TensorFlow Developer Certificate\", \"date\": \"2022.05\", \"issuer\": \"Google\"}, {\"title\": \"TOEIC 990점\", \"date\": \"2021.12\", \"issuer\": \"ETS\"}]";

            String extractedText = "[이름]\n김지능\n\n[희망 직무]\nAI/LLM Engineer\n\n[연락처]\nEmail: ai.engineer@example.com\nPhone: 010-1234-5678\n\n[학력]\n- KAIST 인공지능학과 석사 (2020.03 ~ 2022.02) - 졸업\n  논문: Efficient Fine-tuning Methods for Large Language Models\n- 서울대학교 컴퓨터공학과 학사 (2016.03 ~ 2020.02) - 졸업\n  학점: 4.2/4.5\n\n[경력]\n1. 네이버 클로바 - Senior AI/LLM Engineer (2022.03 ~ 현재, 2년 10개월)\n   - HyperCLOVA X 모델 파인튜닝 및 최적화\n   - RAG 기반 챗봇 시스템 아키텍처 설계 및 구현\n   - Vector DB 성능 최적화 (응답속도 40% 개선)\n   - LLM 프롬프트 엔지니어링 및 Few-shot Learning 적용\n   - MLOps 파이프라인 구축 (모델 배포 자동화)\n\n2. 카카오브레인 - AI Research Engineer 인턴 (2021.06 ~ 2021.12, 6개월)\n   - KoGPT 모델 경량화 연구 (모델 크기 30% 감소)\n   - Transformer 아키텍처 최적화 실험\n   - 한국어 NLP 데이터셋 구축 및 전처리\n\n[프로젝트]\n1. 기업용 AI 챗봇 플랫폼 개발 (2023.01 ~ 2024.12)\n   - LangChain + GPT-4 기반 엔터프라이즈 챗봇 시스템 구축\n   - RAG 파이프라인 설계 및 Vector DB 최적화\n   - 월간 활성 사용자 10만명 달성\n   - 기술스택: Python, LangChain, OpenAI API, Pinecone, FastAPI\n\n2. 오픈소스 LLM 파인튜닝 프레임워크 개발 (2023.06 ~ 2024.03)\n   - LoRA, QLoRA 기반 효율적인 파인튜닝 도구 개발\n   - GitHub Star 2.5k+ 획득\n   - PyPI 다운로드 50만회 이상\n   - 기술스택: Python, PyTorch, Hugging Face Transformers\n\n3. 멀티모달 AI 모델 연구 (2022.09 ~ 2023.05)\n   - CLIP, BLIP 기반 이미지-텍스트 멀티모달 모델 연구\n   - 한국어 데이터셋 구축 및 모델 성능 15% 향상\n   - 국제학회 논문 게재 (NeurIPS 2023)\n\n[보유 기술]\n- 프로그래밍: Python (Expert), C++ (Intermediate)\n- ML/DL 프레임워크: PyTorch, TensorFlow, JAX\n- LLM 도구: Hugging Face Transformers, LangChain, LlamaIndex\n- LLM API: OpenAI API, Anthropic Claude, Google Gemini\n- Vector DB: Pinecone, Weaviate, Chroma, FAISS\n- MLOps: Docker, Kubernetes, AWS SageMaker, MLflow\n- 백엔드: FastAPI, Flask, Django\n- 클라우드: AWS, GCP\n- 기타: RAG, Fine-tuning, Prompt Engineering, RLHF\n\n[자격증 및 어학]\n- AWS Certified Machine Learning - Specialty (2023.08)\n- TensorFlow Developer Certificate (2022.05)\n- TOEIC 990점 (2021.12)\n\n[수상 경력]\n- 네이버 클로바 우수사원상 (2024.06)\n- KAIST AI 해커톤 대상 (2021.11)\n- 서울대 컴퓨터공학과 학술제 최우수상 (2019.12)";

            int updated = jdbcTemplate.update(sql,
                    "AI/LLM 엔지니어 이력서",
                    "AI/LLM",
                    "김지능",
                    "ai.engineer@example.com",
                    "010-1234-5678",
                    skills,
                    educations,
                    careers,
                    experiences,
                    certificates,
                    extractedText,
                    "COMPLETED"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updated", updated);
            response.put("message", "AI/LLM 엔지니어 이력서가 성공적으로 업데이트되었습니다!");

            log.info("✅ AI/LLM 이력서 업데이트 완료: {} rows", updated);

            return response;

        } catch (Exception e) {
            log.error("❌ 이력서 업데이트 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }

    @PostMapping("/create-fullstack-resume")
    public Map<String, Object> createFullstackResume() {
        try {
            String sql = "UPDATE resume SET " +
                    "title = ?, " +
                    "job_category = ?, " +
                    "resume_name = ?, " +
                    "skills = ?, " +
                    "experiences = ?, " +
                    "status = ?, " +
                    "updated_at = NOW() " +
                    "WHERE resume_id = 17";

            String skills = "[\"Java\", \"Spring Boot\", \"JavaScript\", \"React\", \"Node.js\", \"MySQL\", \"Redis\", \"Docker\", \"AWS\", \"TypeScript\"]";

            String experiences = "[" +
                    "{\"title\": \"MSA 기반 이커머스 플랫폼 구축\", " +
                    "\"period\": \"2023.03 ~ 2024.01\", " +
                    "\"description\": \"Spring Cloud를 활용한 마이크로서비스 아키텍처 설계 및 구현. Redis를 사용한 장바구니 성능 50% 개선. Docker/K8s 기반 배포 자동화 구축.\"}, " +
                    "{\"title\": \"실시간 협업 대시보드 개발\", " +
                    "\"period\": \"2022.06 ~ 2022.12\", " +
                    "\"description\": \"React와 Socket.io를 이용한 실시간 데이터 시각화 도구 개발. 복잡한 수치 데이터를 차트화하여 사용자 가독성 향상. 프론트엔드 렌더링 최적화로 화질 저하 없이 응답성 유지.\"}" +
                    "]";

            int updated = jdbcTemplate.update(sql,
                    "풀스택 개발자 이력서",
                    "Fullstack",
                    "이영희",
                    skills,
                    experiences,
                    "COMPLETED"
            );

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("updated", updated);
            response.put("message", "resume_id 17 풀스택 이력서가 성공적으로 업데이트되었습니다!");

            log.info("✅ Fullstack 이력서 업데이트 완료: {} rows", updated);

            return response;

        } catch (Exception e) {
            log.error("❌ 이력서 업데이트 실패", e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", e.getMessage());
            return response;
        }
    }
}
