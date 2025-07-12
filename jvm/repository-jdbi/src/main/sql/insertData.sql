
-- 1. Users
INSERT INTO ps.USER (email, username, passwordHash) VALUES
    ('bob@example.com','bob','$2a$10$YY/UTCDGYAke5hmLGxG0q.wF9jgjcT6qpkdJBtnTk/Ms36sQ.uPaC'),
    ('alice@example.com','alice','$2a$10$YY/UTCDGYAke5hmLGxG0q.wF9jgjcT6qpkdJBtnTk/Ms36sQ.uPaC');

-- 2. Sessions

-- 3. One calendar event each
INSERT INTO ps.EVENT (title, user_id, start_time, end_time) VALUES
                                                                ('Meeting', (SELECT id FROM ps.USER WHERE username='bob'), '2025-05-10 09:00', '2025-05-10 10:00'),
                                                                ('Briefing', (SELECT id FROM ps.USER WHERE username='alice'), '2025-05-10 09:00', '2025-05-10 10:00');
-- 4. One location (ISEL) each
INSERT INTO ps.LOCATION (name, latitude, longitude, radius, user_id) VALUES
    ('ISEL Campus', 38.7569, -9.1165, 50.0, (SELECT id FROM ps.USER WHERE username='bob')),
    ('ISEL Campus', 38.7569, -9.1165, 100.0,(SELECT id FROM ps.USER WHERE username='alice'));

-- 5. Rules: one event rule, one location rule per user
INSERT INTO ps.RULE_EVENT (start_time,end_time, event_id,user_id) VALUES
    ('2025-05-10 09:00','2025-05-10 10:00',(SELECT id FROM ps.EVENT WHERE title='Meeting'),(SELECT id FROM ps.USER WHERE username='bob')),
    ('2025-05-11 14:00','2025-05-11 15:00',(SELECT id FROM ps.EVENT WHERE title='Briefing'),(SELECT id FROM ps.USER WHERE username='alice'));

INSERT INTO ps.RULE_LOCATION (location_id,user_id) VALUES(
        (SELECT id FROM ps.LOCATION WHERE user_id=(SELECT id FROM ps.USER WHERE username='bob')),
        (SELECT id FROM ps.USER WHERE username='bob')
    ),
    ((SELECT id FROM ps.LOCATION WHERE user_id=(SELECT id FROM ps.USER WHERE username='alice')),
        (SELECT id FROM ps.USER WHERE username='alice')
    );
