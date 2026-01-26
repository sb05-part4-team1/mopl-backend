import http from 'k6/http';
import { check } from 'k6'; // sleep 제거 (쉴 틈 안 주기)

export const options = {
    vus: 10,         // 동시 접속자 10명 (로컬이니까 소박하게)
    duration: '300s', // 30초 동안 공격!
};

export default function () {
    // 1. 토큰 설정 (Bearer 뒤에 띄어쓰기 한 칸 필수!)
    const token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIwMTk0MzlhMC0wMDAzLTcwMDAtODAwMC0wMDAwMDAwMDAwMDMiLCJyb2xlIjoiVVNFUiIsImV4cCI6MTc2OTA2MzQ1OSwiaWF0IjoxNzY5MDYxNjU5LCJqdGkiOiI2YWZkOTg4Yi0wZWVhLTRjZmYtOGMwOS1lNjA0Yzk5MjdjMWEifQ.ayJgRU6DQlUVBUCZmiFhnq_GXh9qEJ_p92g2LcUGpOY';

    // 2. 헤더 만들기
    const params = {
        headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
    };

    // 3. 요청 보낼 때 헤더(params) 같이 보내기
    // http.get(주소, 헤더) 형태입니다.
    const res = http.get('http://localhost:8080/api/playlists', params);

    // 4. 에러 로그 출력 (혹시 또 실패하면 원인 보려고)
    if (res.status !== 200) {
        console.log(`🔥 에러! 코드: ${res.status} / 내용: ${res.body}`);
    }

    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    // sleep(1);  <-- 주석 처리! (사람처럼 쉬지 말고 기계처럼 요청 쏘기)
}