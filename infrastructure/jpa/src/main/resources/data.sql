-- =============================================
-- 1. 사용자 (Users)
-- =============================================
INSERT INTO users (id, created_at, deleted_at, updated_at, auth_provider, email, name, password, profile_image_url, role, locked)
VALUES
    (CAST('019439a0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -90, NOW()), NULL, DATEADD('DAY', -90, NOW()), 'EMAIL', 'admin@mopl.com', '이윤수', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0001-7000-8000-000000000001/31a154c4-813f-48de-a144-e554b85396c0_profile.png', 'ADMIN', FALSE),
    (CAST('019439a0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -60, NOW()), NULL, DATEADD('DAY', -5, NOW()), 'EMAIL', 'user1@mopl.com', '김수연', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0002-7000-8000-000000000002/e8b37a36-c447-4a01-8d9c-94f7ce48e949_profile.png', 'USER', FALSE),
    (CAST('019439a0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -45, NOW()), NULL, DATEADD('DAY', -3, NOW()), 'EMAIL', 'user2@mopl.com', '류승민', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0003-7000-8000-000000000003/98954bec-acad-41d5-a559-9e98496b76fa_profile.png', 'USER', FALSE),
    (CAST('019439a0-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -30, NOW()), NULL, DATEADD('DAY', -1, NOW()), 'GOOGLE', 'user3@gmail.com', '박지석', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0004-7000-8000-000000000004/d6daf9d1-fd9c-4910-bb3e-8679dffed59a_profile.png', 'USER', FALSE),
    (CAST('019439a0-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -14, NOW()), NULL, DATEADD('HOUR', -12, NOW()), 'KAKAO', 'user4@kakao.com', '박지성', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0005-7000-8000-000000000005/823a0379-052e-45bc-a975-e1894b53c3b3_profile.png', 'USER', FALSE),
    (CAST('019439a0-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -10, NOW()), NULL, DATEADD('DAY', -2, NOW()), 'EMAIL', 'user5@mopl.com', '임재혁', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0006-7000-8000-000000000006/b475a73f-c33a-41db-bc1e-1c5b3ab50995_profile.png', 'USER', FALSE),
    (CAST('019439a0-0007-7000-8000-000000000007' AS UUID), DATEADD('DAY', -7, NOW()), NULL, DATEADD('DAY', -2, NOW()), 'EMAIL', 'locked@mopl.com', '이해성', '$2a$10$gCRpIif4.HK5xSpWLeNJ8OCGEaAjnI5Rv33SZs5tTb6F7EyMpjjWe', 'http://localhost:8080/api/v1/files/display?path=users/019439a0-0007-7000-8000-000000000007/68f7ad1c-8c35-4ad8-b664-9d104399c066_profile.png', 'USER', TRUE);

-- =============================================
-- 2. 태그 (Tags)
-- =============================================
INSERT INTO tags (id, created_at, deleted_at, name)
VALUES
    (CAST('019439b0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '액션'),
    (CAST('019439b0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '드라마'),
    (CAST('019439b0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -100, NOW()), NULL, 'SF'),
    (CAST('019439b0-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '코미디'),
    (CAST('019439b0-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '스릴러'),
    (CAST('019439b0-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '로맨스'),
    (CAST('019439b0-000a-7000-8000-00000000000a' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '범죄'),
    (CAST('019439b0-000b-7000-8000-00000000000b' AS UUID), DATEADD('DAY', -100, NOW()), NULL, '미스터리');

-- =============================================
-- 3. 콘텐츠 (Contents) - 영화
-- =============================================
INSERT INTO contents (id, created_at, deleted_at, updated_at, type, title, description, thumbnail_url, average_rating, review_count)
VALUES
    (CAST('019439c0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -85, NOW()), NULL, DATEADD('DAY', -85, NOW()), 'MOVIE', '세 가지 색: 레드 (1994)', '발렌틴은 스위스의 제네바 대학 학생이며 파트 타임 모델로 일하고 있다. 그녀는 이웃들의 전화 통화를 도청하는 은퇴한 판사의 개를 부상시킨 후 우정을 쌓기 시작하고, 그 만남은 인생에서의 우연과 인연에 대해 생각하게 만든다.', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0001-7000-8000-000000000001/a1b2c3d4-e5f6-4a7b-8c9d-0e1f2a3b4c5d_thumbnail.webp', 4.75, 2),
    (CAST('019439c0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -82, NOW()), NULL, DATEADD('DAY', -82, NOW()), 'MOVIE', '사랑에 빠진 것처럼 (2012)', '도쿄의 고급스러운 바에서 사랑에 무뎌진 노인과 사랑을 갈망하는 젊은 여자가 만나 서로의 삶에 대한 관점에 영향을 주고받는다.', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0002-7000-8000-000000000002/b2c3d4e5-f6a7-4b8c-9d0e-1f2a3b4c5d6e_thumbnail.webp', 0.0, 0),
    (CAST('019439c0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -78, NOW()), NULL, DATEADD('DAY', -78, NOW()), 'MOVIE', '중경삼림 (1994)', '경찰 223은 헤어진 옛 애인을 기다리며, 1달 동안 그녀에게서 연락이 오지 않으면 그녀를 잊기로 마음먹는다. 같은 시간, 마약 딜러는 자신을 배신한 마약 중개인을 제거한 뒤 술집을 찾고 그곳에서 경찰 223은 술집으로 처음 들어오는 여자를 사랑하겠노라 마음먹는다. 한편 패스트푸드점에서 일하는 점원 페이는 언제나처럼 똑같은 샐러드를 고른 경찰 663을 남몰래 좋아하고 있다. 어느 날, 경찰 663의 애인이 이별의 편지와 함께 경찰 663의 아파트 열쇠를 페이의 가게에게 맡긴다. 페이는 경찰 663이 집을 비운 사이 남아있는 그녀의 흔적을 하나 둘 지워나가는데...', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0003-7000-8000-000000000003/c3d4e5f6-a7b8-4c9d-0e1f-2a3b4c5d6e7f_thumbnail.webp', 5.0, 1),
    (CAST('019439c0-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -72, NOW()), NULL, DATEADD('DAY', -72, NOW()), 'MOVIE', '테넷 (2020)', '주도자는 미국의 한 요원으로 우크라이나 국립 오페라 극장의 한 사건에 투입되었다가 우크라이나 요원들에게 붙잡히게 되고 고문을 받지만 CIA가 준 자살 약을 먹고 자살을 택하게 된다. 그러나 이내 다시 눈을 뜬 주인공은 의문의 한 남자로부터 임무를 부여받는다. 그가 주도자에게 줄 수 있는 건 하나의 제스처와 하나의 단어 뿐. 시간의 흐름을 뒤집는 인버전을 통해 현재와 미래를 오가며 세상을 파괴하려는 사토르를 막기 위해 투입된 작전의 주도자는 인버전에 대한 정보를 가진 닐과 미술품 감정사이자 사토르에 대한 복수심이 가득한 그의 아내 캣과 협력해 미래의 공격에 맞서 제3차 세계대전을 막아야 한다.', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0004-7000-8000-000000000004/d4e5f6a7-b8c9-4d0e-1f2a-3b4c5d6e7f8a_thumbnail.jpg', 4.5, 1),
    (CAST('019439c0-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -68, NOW()), NULL, DATEADD('DAY', -68, NOW()), 'MOVIE', '펀치 드렁크 러브 (2002)', '7명이나 되는 누나들한테 들들 볶이며 자란 배리. 비행 마일리지를 경품으로 준다는 푸딩을 사모으는 것이 유일한 낙인 그는 어느 날 아침 거리에 내동댕이 쳐진 낡은 풍금을 발견하곤 사무실에 가져다 놓는다. 그리고 바로 그날, 뜻하지 않게 신비로운 여인 레나를 만나게 된다. 오래 전부터 당신을 사랑해 왔다고, 당신과 키스하고 싶다고 말하는 레나와 순식간에 사랑에 빠지는 배리. 하지만 일생에 단 한번 올까 말까한 가슴벅찬 사랑을 방해하는 것이 있다. 다름아닌 외로움에 지쳐 폰 섹스를 걸었다가 알게 된 악덕업체 일당, 일명 매트리스 맨. 배리와 레나가 꿈결 같은여행에서 돌아오던 날, 특별한 손님들이 그들을 기다리는데...', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0005-7000-8000-000000000005/e5f6a7b8-c9d0-4e1f-2a3b-4c5d6e7f8a9b_thumbnail.webp', 0.0, 0),
    (CAST('019439c0-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -62, NOW()), NULL, DATEADD('DAY', -62, NOW()), 'MOVIE', '하나비 (1997)', '형사인 니시(기타노 다케시)와 호리베(렌 오스기)는 야쿠자 소탕 전문형사 콤비이자 친한 친구 사이. 그러던 어느 날, 니시는 딸을 잃고 아내가 시한부 판정까지 받게 된다. 잠복 근무 중 동료들의 호의로 니시가 아내의 병문안을 간 사이 불의의 습격을 받은 호리에는 불구가 되어 가족들에게 버림을 받는다. 게다가 호리에를 습격한 범인이 니시를 따르던 후배 경찰까지 죽이자 이에 분노한 니시는 그 자리에서 마지막 총알까지 다 퍼부으면서 범인을 죽인 후 경찰직을 그만둔다. 한편 아내의 치료비 때문에 큰 빚을 지게 된 니시는 경찰을 사칭하여 은행을 터는데...', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0006-7000-8000-000000000006/f6a7b8c9-d0e1-4f2a-3b4c-5d6e7f8a9b0c_thumbnail.webp', 5.0, 1),
    (CAST('019439c0-0007-7000-8000-000000000007' AS UUID), DATEADD('DAY', -58, NOW()), NULL, DATEADD('DAY', -58, NOW()), 'MOVIE', '퐁네프의 연인들 (1991)', '파리 센느강의 아홉 번째 다리 퐁네프. 사랑을 잃고 거리를 방황하며 그림을 그리는 여자 미셸, 폐쇄된 퐁네프 다리 위에서 처음 만난 그녀가 삶의 전부인 남자 알렉스. 마치 내일이 없는 듯 열정적이고 치열하게 사랑한 두 사람. 한 때 서로가 전부였던 그들은 3년 뒤, 크리스마스에 퐁네프의 다리에서 재회하기로 하는데...', 'http://localhost:8080/api/v1/files/display?path=contents/019439c0-0007-7000-8000-000000000007/a7b8c9d0-e1f2-4a3b-4c5d-6e7f8a9b0c1d_thumbnail.jpg', 4.25, 2);

-- =============================================
-- 4. 콘텐츠-태그 연결 (Content Tags)
-- =============================================
INSERT INTO content_tags (id, created_at, deleted_at, content_id, tag_id)
VALUES
    -- 세 가지 색: 레드 - 드라마, 미스터리, 로맨스
    (CAST('019439d0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -85, NOW()), NULL, CAST('019439c0-0001-7000-8000-000000000001' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439d0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -85, NOW()), NULL, CAST('019439c0-0001-7000-8000-000000000001' AS UUID), CAST('019439b0-000b-7000-8000-00000000000b' AS UUID)),
    (CAST('019439d0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -85, NOW()), NULL, CAST('019439c0-0001-7000-8000-000000000001' AS UUID), CAST('019439b0-0006-7000-8000-000000000006' AS UUID)),
    -- 사랑에 빠진 것처럼 - 드라마
    (CAST('019439d0-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -82, NOW()), NULL, CAST('019439c0-0002-7000-8000-000000000002' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    -- 중경삼림 - 드라마, 코미디, 로맨스
    (CAST('019439d0-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -78, NOW()), NULL, CAST('019439c0-0003-7000-8000-000000000003' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439d0-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -78, NOW()), NULL, CAST('019439c0-0003-7000-8000-000000000003' AS UUID), CAST('019439b0-0004-7000-8000-000000000004' AS UUID)),
    (CAST('019439d0-0007-7000-8000-000000000007' AS UUID), DATEADD('DAY', -78, NOW()), NULL, CAST('019439c0-0003-7000-8000-000000000003' AS UUID), CAST('019439b0-0006-7000-8000-000000000006' AS UUID)),
    -- 테넷 - 액션, 스릴러, SF
    (CAST('019439d0-0008-7000-8000-000000000008' AS UUID), DATEADD('DAY', -72, NOW()), NULL, CAST('019439c0-0004-7000-8000-000000000004' AS UUID), CAST('019439b0-0001-7000-8000-000000000001' AS UUID)),
    (CAST('019439d0-0009-7000-8000-000000000009' AS UUID), DATEADD('DAY', -72, NOW()), NULL, CAST('019439c0-0004-7000-8000-000000000004' AS UUID), CAST('019439b0-0005-7000-8000-000000000005' AS UUID)),
    (CAST('019439d0-000a-7000-8000-00000000000a' AS UUID), DATEADD('DAY', -72, NOW()), NULL, CAST('019439c0-0004-7000-8000-000000000004' AS UUID), CAST('019439b0-0003-7000-8000-000000000003' AS UUID)),
    -- 펀치 드렁크 러브 - 로맨스, 드라마, 코미디
    (CAST('019439d0-000b-7000-8000-00000000000b' AS UUID), DATEADD('DAY', -68, NOW()), NULL, CAST('019439c0-0005-7000-8000-000000000005' AS UUID), CAST('019439b0-0006-7000-8000-000000000006' AS UUID)),
    (CAST('019439d0-000c-7000-8000-00000000000c' AS UUID), DATEADD('DAY', -68, NOW()), NULL, CAST('019439c0-0005-7000-8000-000000000005' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439d0-000d-7000-8000-00000000000d' AS UUID), DATEADD('DAY', -68, NOW()), NULL, CAST('019439c0-0005-7000-8000-000000000005' AS UUID), CAST('019439b0-0004-7000-8000-000000000004' AS UUID)),
    -- 하나비 - 범죄, 드라마
    (CAST('019439d0-000e-7000-8000-00000000000e' AS UUID), DATEADD('DAY', -62, NOW()), NULL, CAST('019439c0-0006-7000-8000-000000000006' AS UUID), CAST('019439b0-000a-7000-8000-00000000000a' AS UUID)),
    (CAST('019439d0-000f-7000-8000-00000000000f' AS UUID), DATEADD('DAY', -62, NOW()), NULL, CAST('019439c0-0006-7000-8000-000000000006' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    -- 퐁네프의 연인들 - 드라마, 로맨스
    (CAST('019439d0-0010-7000-8000-000000000010' AS UUID), DATEADD('DAY', -58, NOW()), NULL, CAST('019439c0-0007-7000-8000-000000000007' AS UUID), CAST('019439b0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439d0-0011-7000-8000-000000000011' AS UUID), DATEADD('DAY', -58, NOW()), NULL, CAST('019439c0-0007-7000-8000-000000000007' AS UUID), CAST('019439b0-0006-7000-8000-000000000006' AS UUID));

-- =============================================
-- 5. 플레이리스트 (Playlists)
-- =============================================
INSERT INTO playlists (id, created_at, deleted_at, updated_at, title, description, owner_id)
VALUES
    (CAST('019439e0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -55, NOW()), NULL, DATEADD('DAY', -10, NOW()), '아트하우스 영화 컬렉션', '예술적 감성이 담긴 영화들', CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439e0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -50, NOW()), NULL, DATEADD('DAY', -8, NOW()), '왕가위 & 아시아 영화', '아시아 감독들의 명작 모음', CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439e0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -40, NOW()), NULL, DATEADD('DAY', -5, NOW()), '로맨스 영화 명작', '가슴 뭉클한 로맨스 영화들', CAST('019439a0-0003-7000-8000-000000000003' AS UUID));

-- =============================================
-- 6. 플레이리스트-콘텐츠 연결 (Playlist Contents)
-- =============================================
INSERT INTO playlist_contents (id, created_at, deleted_at, playlist_id, content_id)
VALUES
    -- 아트하우스 영화 컬렉션: 세 가지 색: 레드, 펀치 드렁크 러브
    (CAST('019439f0-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -55, NOW()), NULL, CAST('019439e0-0001-7000-8000-000000000001' AS UUID), CAST('019439c0-0001-7000-8000-000000000001' AS UUID)),
    (CAST('019439f0-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -48, NOW()), NULL, CAST('019439e0-0001-7000-8000-000000000001' AS UUID), CAST('019439c0-0005-7000-8000-000000000005' AS UUID)),
    -- 왕가위 & 아시아 영화: 중경삼림, 사랑에 빠진 것처럼, 하나비
    (CAST('019439f0-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -50, NOW()), NULL, CAST('019439e0-0002-7000-8000-000000000002' AS UUID), CAST('019439c0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('019439f0-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -45, NOW()), NULL, CAST('019439e0-0002-7000-8000-000000000002' AS UUID), CAST('019439c0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('019439f0-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -40, NOW()), NULL, CAST('019439e0-0002-7000-8000-000000000002' AS UUID), CAST('019439c0-0006-7000-8000-000000000006' AS UUID)),
    -- 로맨스 영화 명작: 퐁네프의 연인들, 중경삼림
    (CAST('019439f0-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -40, NOW()), NULL, CAST('019439e0-0003-7000-8000-000000000003' AS UUID), CAST('019439c0-0007-7000-8000-000000000007' AS UUID)),
    (CAST('019439f0-0007-7000-8000-000000000007' AS UUID), DATEADD('DAY', -35, NOW()), NULL, CAST('019439e0-0003-7000-8000-000000000003' AS UUID), CAST('019439c0-0003-7000-8000-000000000003' AS UUID));

-- =============================================
-- 7. 플레이리스트 구독자 (Playlist Subscribers)
-- =============================================
INSERT INTO playlist_subscribers (id, created_at, deleted_at, playlist_id, subscriber_id)
VALUES
    (CAST('01943a00-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -42, NOW()), NULL, CAST('019439e0-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a00-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -28, NOW()), NULL, CAST('019439e0-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    (CAST('01943a00-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -38, NOW()), NULL, CAST('019439e0-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a00-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -35, NOW()), NULL, CAST('019439e0-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID));

-- =============================================
-- 8. 팔로우 (Follows)
-- =============================================
INSERT INTO follows (id, created_at, deleted_at, follower_id, followee_id)
VALUES
    -- user1 -> user2, user3
    (CAST('01943a10-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -50, NOW()), NULL, CAST('019439a0-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a10-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -25, NOW()), NULL, CAST('019439a0-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    -- user2 -> user1
    (CAST('01943a10-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -40, NOW()), NULL, CAST('019439a0-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    -- user3 -> user1, user4
    (CAST('01943a10-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -20, NOW()), NULL, CAST('019439a0-0004-7000-8000-000000000004' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a10-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -12, NOW()), NULL, CAST('019439a0-0004-7000-8000-000000000004' AS UUID), CAST('019439a0-0005-7000-8000-000000000005' AS UUID)),
    -- user4 -> user1
    (CAST('01943a10-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -8, NOW()), NULL, CAST('019439a0-0005-7000-8000-000000000005' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID));

-- =============================================
-- 9. 리뷰 (Reviews)
-- =============================================
INSERT INTO reviews (id, created_at, deleted_at, updated_at, text, rating, content_id, author_id)
VALUES
    -- 세 가지 색: 레드 리뷰
    (CAST('01943a20-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -45, NOW()), NULL, DATEADD('DAY', -45, NOW()), '키에슬로프스키의 삼색 3부작 중 최고작. 우연과 운명에 대한 깊은 통찰이 담겨 있어요.', 5.0, CAST('019439c0-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a20-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -30, NOW()), NULL, DATEADD('DAY', -30, NOW()), '이레느 야콥의 연기가 인상적입니다. 볼 때마다 새로운 의미가 보여요.', 4.5, CAST('019439c0-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    -- 중경삼림 리뷰
    (CAST('01943a20-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -22, NOW()), NULL, DATEADD('DAY', -22, NOW()), '왕가위 감독의 독특한 영상미와 음악이 어우러진 걸작!', 5.0, CAST('019439c0-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    -- 테넷 리뷰
    (CAST('01943a20-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -18, NOW()), NULL, DATEADD('DAY', -18, NOW()), '놀란 감독의 시간 역행 아이디어가 놀랍습니다. 두세 번은 봐야 이해가 돼요.', 4.5, CAST('019439c0-0004-7000-8000-000000000004' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    -- 퐁네프의 연인들 리뷰
    (CAST('01943a20-0005-7000-8000-000000000005' AS UUID), DATEADD('DAY', -7, NOW()), NULL, DATEADD('DAY', -7, NOW()), '파리의 밤과 불꽃놀이 장면이 너무 아름다워요. 뜨거운 사랑 이야기.', 4.5, CAST('019439c0-0007-7000-8000-000000000007' AS UUID), CAST('019439a0-0005-7000-8000-000000000005' AS UUID)),
    (CAST('01943a20-0006-7000-8000-000000000006' AS UUID), DATEADD('DAY', -4, NOW()), NULL, DATEADD('DAY', -4, NOW()), '카락스 감독과 줄리엣 비노쉬의 조합이 환상적이에요.', 4.0, CAST('019439c0-0007-7000-8000-000000000007' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    -- 하나비 리뷰
    (CAST('01943a20-0007-7000-8000-000000000007' AS UUID), DATEADD('HOUR', -18, NOW()), NULL, DATEADD('HOUR', -18, NOW()), '기타노 다케시의 폭력과 서정이 공존하는 걸작. 베니스 황금사자상 수상작답습니다.', 5.0, CAST('019439c0-0006-7000-8000-000000000006' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID));

-- =============================================
-- 10. 알림 (Notifications)
-- =============================================
INSERT INTO notifications (id, created_at, deleted_at, title, content, level, receiver_id)
VALUES
    (CAST('01943a30-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -60, NOW()), NULL, '환영합니다!', 'MOPL에 가입해주셔서 감사합니다.', 'INFO', CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a30-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -40, NOW()), NULL, '새로운 팔로워', '류승민님이 회원님을 팔로우하기 시작했습니다.', 'INFO', CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a30-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -5, NOW()), NULL, '플레이리스트 업데이트', '구독 중인 플레이리스트에 새로운 영화가 추가되었습니다.', 'INFO', CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a30-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -1, NOW()), NULL, '서비스 점검 안내', '내일 오전 2시-4시 서비스 점검이 예정되어 있습니다.', 'WARNING', CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a30-0005-7000-8000-000000000005' AS UUID), DATEADD('HOUR', -3, NOW()), NULL, '새로운 리뷰', '회원님의 플레이리스트에 새로운 리뷰가 작성되었습니다.', 'INFO', CAST('019439a0-0003-7000-8000-000000000003' AS UUID));

-- =============================================
-- 11. 대화 (Conversations)
-- =============================================
INSERT INTO conversations (id, created_at, deleted_at, updated_at)
VALUES
    (CAST('01943a40-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -30, NOW()), NULL, DATEADD('HOUR', -2, NOW())),
    (CAST('01943a40-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -15, NOW()), NULL, DATEADD('HOUR', -1, NOW())),
    (CAST('01943a40-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -10, NOW()), NULL, DATEADD('HOUR', -3, NOW()));

-- =============================================
-- 12. 다이렉트 메시지 (Direct Messages)
-- =============================================
INSERT INTO direct_messages (id, created_at, deleted_at, content, conversation_id, sender_id)
VALUES
    -- 대화 1: user1 <-> user2
    (CAST('01943a50-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -30, NOW()), NULL, '안녕하세요! 아트하우스 영화 플레이리스트 잘 봤어요.', CAST('01943a40-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a50-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -29, NOW()), NULL, '감사합니다! 하나비도 추가할 예정이에요.', CAST('01943a40-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a50-0003-7000-8000-000000000003' AS UUID), DATEADD('HOUR', -2, NOW()), NULL, '혹시 세 가지 색 시리즈 다 보셨나요?', CAST('01943a40-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    -- 대화 2: user1 <-> user3
    (CAST('01943a50-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -15, NOW()), NULL, '영화 같이 보러 가실래요?', CAST('01943a40-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    (CAST('01943a50-0005-7000-8000-000000000005' AS UUID), DATEADD('HOUR', -1, NOW()), NULL, '좋아요! 어떤 영화 보고 싶으세요?', CAST('01943a40-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    -- 대화 3: user2 <-> user3
    (CAST('01943a50-0004-7000-8000-000000000006' AS UUID), DATEADD('HOUR', -3, NOW()), NULL, '안녕하세요?', CAST('01943a40-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    (CAST('01943a50-0005-7000-8000-000000000007' AS UUID), DATEADD('HOUR', -1, NOW()), NULL, '안녕하세요!', CAST('01943a40-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID));


-- =============================================
-- 13. 읽음 상태 (Read Statuses)
-- =============================================
INSERT INTO read_statuses (id, created_at, deleted_at, last_read_at, conversation_id, participant_id)
VALUES
    -- 대화 1 참여자들
    (CAST('01943a60-0001-7000-8000-000000000001' AS UUID), DATEADD('DAY', -30, NOW()), NULL, NOW(), CAST('01943a40-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a60-0002-7000-8000-000000000002' AS UUID), DATEADD('DAY', -30, NOW()), NULL, DATEADD('DAY', -29, NOW()), CAST('01943a40-0001-7000-8000-000000000001' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    -- 대화 2 참여자들
    (CAST('01943a60-0003-7000-8000-000000000003' AS UUID), DATEADD('DAY', -15, NOW()), NULL, NOW(), CAST('01943a40-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0002-7000-8000-000000000002' AS UUID)),
    (CAST('01943a60-0004-7000-8000-000000000004' AS UUID), DATEADD('DAY', -15, NOW()), NULL, DATEADD('HOUR', -1, NOW()), CAST('01943a40-0002-7000-8000-000000000002' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID)),
    -- 대화 3 참여자들
    (CAST('01943a60-0001-7000-8000-000000000005' AS UUID), DATEADD('DAY', -10, NOW()), NULL, NOW(), CAST('01943a40-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0003-7000-8000-000000000003' AS UUID)),
    (CAST('01943a60-0002-7000-8000-000000000006' AS UUID), DATEADD('DAY', -10, NOW()), NULL, DATEADD('HOUR', -1, NOW()), CAST('01943a40-0003-7000-8000-000000000003' AS UUID), CAST('019439a0-0004-7000-8000-000000000004' AS UUID));
