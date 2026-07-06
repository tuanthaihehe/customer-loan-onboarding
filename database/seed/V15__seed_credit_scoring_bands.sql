-- Seed data for simple credit scoring bands
-- Based on Logic tính điểm Scoring.xlsx
-- Income values are stored in VND, not million VND.

DELETE FROM overall_score_grade_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM dependent_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM age_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';
DELETE FROM income_score_band WHERE rule_set_code = 'BASIC_SCORING_V1';

INSERT INTO income_score_band
(rule_set_code, min_income_amount, max_income_amount, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 0,        5000000,  10, 0.3000, 'Từ 0 đến dưới 5 triệu', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 5000000,  10000000, 30, 0.3000, 'Từ 5 đến dưới 10 triệu', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 10000000, 15000000, 50, 0.3000, 'Từ 10 đến dưới 15 triệu', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 15000000, 20000000, 70, 0.3000, 'Từ 15 đến dưới 20 triệu', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 20000000, NULL,     100, 0.3000, 'Từ 20 triệu trở lên', 5, CURRENT_DATE);

INSERT INTO age_score_band
(rule_set_code, min_age, max_age, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 18, 25,   40, 0.4000, 'Từ 18 đến dưới 25 tuổi', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 25, 30,   60, 0.4000, 'Từ 25 đến dưới 30 tuổi', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 30, 40,   80, 0.4000, 'Từ 30 đến dưới 40 tuổi', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 40, NULL, 90, 0.4000, 'Từ 40 tuổi trở lên', 4, CURRENT_DATE);

INSERT INTO dependent_score_band
(rule_set_code, min_dependent_count, max_dependent_count, score_value, weight, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 0, 1,    100, 0.3000, '0 người phụ thuộc', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 1, 2,    80,  0.3000, '1 người phụ thuộc', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 2, 3,    60,  0.3000, '2 người phụ thuộc', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 3, 4,    40,  0.3000, '3 người phụ thuộc', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 4, NULL, 20,  0.3000, 'Từ 4 người phụ thuộc trở lên', 5, CURRENT_DATE);

INSERT INTO overall_score_grade_band
(rule_set_code, grade_code, min_score, max_score, display_label, priority, effective_from)
VALUES
('BASIC_SCORING_V1', 'A', 80, NULL, 'Rất tốt', 1, CURRENT_DATE),
('BASIC_SCORING_V1', 'B', 60, 80,   'Tốt', 2, CURRENT_DATE),
('BASIC_SCORING_V1', 'C', 40, 60,   'Trung bình', 3, CURRENT_DATE),
('BASIC_SCORING_V1', 'D', 20, 40,   'Rủi ro cao', 4, CURRENT_DATE),
('BASIC_SCORING_V1', 'E', 0,  20,   'Rủi ro rất cao', 5, CURRENT_DATE);
