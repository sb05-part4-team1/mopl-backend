import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// 커스텀 메트릭
const errorRate = new Rate('errors');
const responseTime = new Trend('response_time');

export const options = {
    // 시나리오 기반 설정
    scenarios: {
        // 기본 부하 테스트
        load_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 10 },  // 30초 동안 10명까지 증가
                { duration: '1m', target: 10 },   // 1분 동안 10명 유지
                { duration: '30s', target: 0 },   // 30초 동안 0명으로 감소
            ],
            gracefulRampDown: '10s',
        },
    },
    // 임계값 설정
    thresholds: {
        http_req_duration: ['p(95)<500'],  // 95% 요청이 500ms 이내
        errors: ['rate<0.1'],               // 에러율 10% 미만
    },
};

// 환경변수에서 토큰 읽기 (없으면 기본값)
const TOKEN = __ENV.K6_TOKEN || '';
const BASE_URL = __ENV.K6_BASE_URL || 'http://localhost:8080';

export default function () {
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // 토큰이 있으면 Authorization 헤더 추가
    if (TOKEN) {
        params.headers['Authorization'] = `Bearer ${TOKEN}`;
    }

    const res = http.get(`${BASE_URL}/api/playlists`, params);

    // 메트릭 기록
    responseTime.add(res.timings.duration);
    errorRate.add(res.status !== 200);

    // 에러 로그
    if (res.status !== 200) {
        console.log(`Error: ${res.status} - ${res.body}`);
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
        'response time < 500ms': (r) => r.timings.duration < 500,
    });

    sleep(1);
}