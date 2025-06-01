
-- 1. Users
INSERT INTO ps.USER (email, username, passwordHash) VALUES
    ('bob@example.com','bob','$2a$10$YY/UTCDGYAke5hmLGxG0q.wF9jgjcT6qpkdJBtnTk/Ms36sQ.uPaC'),
    ('alice@example.com','alice','$2a$10$YY/UTCDGYAke5hmLGxG0q.wF9jgjcT6qpkdJBtnTk/Ms36sQ.uPaC');

-- 2. Sessions
INSERT INTO ps.SESSION (user_id)
SELECT id FROM ps.USER;

-- 3. One calendar event each
INSERT INTO ps.EVENT (calendar_id, event_id, title, user_id) VALUES
    (1,  1001, 'Bob Meeting',   (SELECT id FROM ps.USER WHERE username='bob')),
    (2,  2001, 'Alice Briefing',(SELECT id FROM ps.USER WHERE username='alice'));

-- 4. One location (ISEL) each
INSERT INTO ps.LOCATION (name, latitude, longitude, user_id) VALUES
    ('ISEL Campus', 38.7569, -9.1165, (SELECT id FROM ps.USER WHERE username='bob')),
    ('ISEL Campus', 38.7569, -9.1165, (SELECT id FROM ps.USER WHERE username='alice'));

-- 5. Exceptions: app and contact
INSERT INTO ps.EXCEPTION_APP     (app_name, user_id) VALUES
    ('Slack', (SELECT id FROM ps.USER WHERE username='bob')),
    ('Slack', (SELECT id FROM ps.USER WHERE username='alice'));

INSERT INTO ps.EXCEPTION_CONTACT (name, phone_number, user_id) VALUES
    ('Eve','555-0100',(SELECT id FROM ps.USER WHERE username='bob')),
    ('Eve','555-0200',(SELECT id FROM ps.USER WHERE username='alice'));

-- 6. Rules: one event rule, one location rule per user
INSERT INTO ps.RULE_EVENT (start_time,end_time,calendar_id,event_id,user_id) VALUES
    ('2025-05-10 09:00','2025-05-10 10:00',1,1001,(SELECT id FROM ps.USER WHERE username='bob')),
    ('2025-05-11 14:00','2025-05-11 15:00',2,2001,(SELECT id FROM ps.USER WHERE username='alice'));

INSERT INTO ps.RULE_LOCATION (start_time,end_time,location_id,user_id) VALUES
    ('2025-05-12 08:00','2025-05-12 12:00',
        (SELECT id FROM ps.LOCATION WHERE user_id=(SELECT id FROM ps.USER WHERE username='bob')),
        (SELECT id FROM ps.USER WHERE username='bob')
    ),
    ('2025-05-13 13:00','2025-05-13 17:00',
        (SELECT id FROM ps.LOCATION WHERE user_id=(SELECT id FROM ps.USER WHERE username='alice')),
        (SELECT id FROM ps.USER WHERE username='alice')
    );

-- 7. Link exceptions to rules
INSERT INTO ps.EXCEPTION_APP_RULE_EVENT      (exception_id, rule_id)
SELECT ea.id, re.id FROM ps.EXCEPTION_APP ea JOIN ps.RULE_EVENT re USING (user_id);

INSERT INTO ps.EXCEPTION_CONTACT_RULE_EVENT  (exception_id, rule_id)
SELECT ec.id, re.id FROM ps.EXCEPTION_CONTACT ec JOIN ps.RULE_EVENT re USING (user_id);

INSERT INTO ps.EXCEPTION_APP_RULE_LOCATION   (exception_id, rule_id)
SELECT ea.id, rl.id FROM ps.EXCEPTION_APP ea JOIN ps.RULE_LOCATION rl USING (user_id);

INSERT INTO ps.EXCEPTION_CONTACT_RULE_LOCATION (exception_id, rule_id)
SELECT ec.id, rl.id FROM ps.EXCEPTION_CONTACT ec JOIN ps.RULE_LOCATION rl USING (user_id);

