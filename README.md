# park-pro-backend-v2
### Database Structure
```sql
-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Park table
CREATE TABLE park
(
    id          UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    name        VARCHAR(100) NOT NULL,
    location    VARCHAR(255) NOT NULL,
    picture     VARCHAR(100),
    description TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_park_name UNIQUE (name)
);

-- User table
CREATE TABLE "user"
(
    id                   UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    first_name           VARCHAR(50)  NOT NULL,
    last_name            VARCHAR(50)  NOT NULL,
    email                VARCHAR(100) NOT NULL UNIQUE,
    password             VARCHAR(255) NOT NULL,
    phone                VARCHAR(15),
    gender               VARCHAR(30),
    passport_national_id VARCHAR(30),
    nationality          VARCHAR(30),
    age                  INTEGER               DEFAULT 18,
    role                 VARCHAR(30)  NOT NULL CHECK (role IN ('ADMIN', 'FINANCE_OFFICER', 'PARK_MANAGER', 'VISITOR',
                                                               'GOVERNMENT_OFFICER', 'AUDITOR')),
    park_id              UUID         REFERENCES park (id) ON DELETE SET NULL,
    is_active            BOOLEAN      NOT NULL DEFAULT FALSE,
    must_reset_password  BOOLEAN      NOT NULL DEFAULT FALSE,
    last_login           TIMESTAMP,
    created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Verification Token table
CREATE TABLE verification_token
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token       VARCHAR(6) NOT NULL,
    user_id     UUID       NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP  NOT NULL,
    CONSTRAINT unique_verification_token UNIQUE (token, user_id)
);

-- Password Reset Token table
CREATE TABLE password_reset_token
(
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    token       VARCHAR(36) NOT NULL,
    user_id     UUID        NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    expiry_date TIMESTAMP   NOT NULL,
    CONSTRAINT unique_reset_token UNIQUE (token)
);

-- Activity table
CREATE TABLE activity
(
    id               UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    name             VARCHAR(100)   NOT NULL,
    park_id          UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    price            DECIMAL(15, 2) NOT NULL CHECK (price >= 0),
    description      TEXT,
    picture          VARCHAR(255),
    capacity_per_day INTEGER CHECK (capacity_per_day >= 0),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Budget table
CREATE TABLE budget
(
    id           UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    park_id      UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    fiscal_year  INTEGER        NOT NULL,
    total_amount DECIMAL(15, 2) NOT NULL CHECK (total_amount >= 0),
    balance      DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
    unallocated  DECIMAL(15, 2) NOT NULL CHECK (unallocated >= 0),
    status       VARCHAR(20)    NOT NULL CHECK (status IN ('DRAFT', 'APPROVED', 'REJECTED')),
    created_by   UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    approved_by  UUID           REFERENCES "user" (id) ON DELETE SET NULL,
    approved_at  TIMESTAMP,
    created_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_park_year UNIQUE (park_id, fiscal_year)
);

-- Income Stream table
CREATE TABLE income_stream
(
    id                 UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    budget_id          UUID           NOT NULL REFERENCES budget (id) ON DELETE CASCADE,
    park_id            UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    fiscal_year        INTEGER        NOT NULL,
    name               VARCHAR(100)   NOT NULL,
    percentage         DECIMAL(5, 2)  NOT NULL CHECK (percentage >= 0 AND percentage <= 100),
    total_contribution DECIMAL(15, 2) NOT NULL CHECK (total_contribution >= 0),
    actual_balance     DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (actual_balance >= 0),
    created_by         UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_income_stream_budget_name UNIQUE (budget_id, name)
);

-- Budget Category table
CREATE TABLE budget_category
(
    id               UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    budget_id        UUID           NOT NULL REFERENCES budget (id) ON DELETE CASCADE,
    name             VARCHAR(100)   NOT NULL,
    allocated_amount DECIMAL(15, 2) NOT NULL CHECK (allocated_amount >= 0),
    used_amount      DECIMAL(15, 2) NOT NULL DEFAULT 0 CHECK (used_amount >= 0),
    balance          DECIMAL(15, 2) NOT NULL CHECK (balance >= 0),
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Expense table
CREATE TABLE expense
(
    id                 UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    budget_id          UUID           NOT NULL REFERENCES budget (id) ON DELETE CASCADE,
    amount             DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    description        TEXT           NOT NULL,
    budget_category_id UUID           NOT NULL REFERENCES budget_category (id) ON DELETE CASCADE,
    park_id            UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    created_by         UUID           REFERENCES "user" (id) ON DELETE SET NULL,
    audit_status       VARCHAR(20)    NOT NULL CHECK (audit_status IN ('PASSED', 'FAILED', 'UNJUSTIFIED')),
    receipt_url        VARCHAR(255),
    currency           VARCHAR(3)     NOT NULL DEFAULT 'XAF',
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Withdraw Request table
CREATE TABLE withdraw_request
(
    id                 UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    amount             DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    reason             TEXT           NOT NULL,
    description        TEXT,
    requester_id       UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    approver_id        UUID           REFERENCES "user" (id) ON DELETE SET NULL,
    budget_category_id UUID           NOT NULL REFERENCES budget_category (id) ON DELETE CASCADE,
    budget_id          UUID           NOT NULL REFERENCES budget (id) ON DELETE CASCADE,
    receipt_url        VARCHAR(255),
    status             VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    audit_status       VARCHAR(20)    NOT NULL DEFAULT 'UNJUSTIFIED' CHECK (audit_status IN ('PASSED', 'FAILED', 'UNJUSTIFIED')),
    approved_at        TIMESTAMP,
    rejection_reason   TEXT,
    park_id            UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    currency           VARCHAR(3)     NOT NULL DEFAULT 'XAF',
    created_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Funding Request table
CREATE TABLE funding_request
(
    id               UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    park_id          UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    budget_id        UUID           NOT NULL REFERENCES budget (id) ON DELETE CASCADE,
    budget_category_id UUID         NOT NULL REFERENCES budget_category (id) ON DELETE CASCADE,
    requested_amount DECIMAL(15, 2) NOT NULL CHECK (requested_amount > 0),
    approved_amount  DECIMAL(15, 2) CHECK (approved_amount >= 0),
    request_type     VARCHAR(20)    NOT NULL CHECK (request_type IN ('EXTRA_FUNDS', 'EMERGENCY_RELIEF')),
    reason           TEXT           NOT NULL,
    requester_id     UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    approver_id      UUID           REFERENCES "user" (id) ON DELETE SET NULL,
    status           VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason TEXT,
    approved_at      TIMESTAMP,
    currency         VARCHAR(3)     NOT NULL DEFAULT 'XAF',
    created_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Donation table
CREATE TABLE donation
(
    id                UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    donor_id          UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    park_id           UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    amount            DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    status            VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    payment_reference VARCHAR(100),
    fiscal_year       INTEGER        NOT NULL,
    currency          VARCHAR(3)     NOT NULL DEFAULT 'XAF',
    confirmed_at      TIMESTAMP,
    created_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Booking table
CREATE TABLE booking
(
    id                       UUID PRIMARY KEY        DEFAULT uuid_generate_v4(),
    visitor_id               UUID           NOT NULL REFERENCES "user" (id) ON DELETE SET NULL,
    activity_id              UUID           NOT NULL REFERENCES activity (id) ON DELETE CASCADE,
    amount                   DECIMAL(15, 2) NOT NULL CHECK (amount > 0),
    park_id                  UUID           NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    visit_date               DATE           NOT NULL,
    status                   VARCHAR(20)    NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED')),
    payment_reference        VARCHAR(100),
    currency                 VARCHAR(3)     NOT NULL DEFAULT 'XAF',
    stripe_payment_intent_id VARCHAR(255),
    stripe_payment_status    VARCHAR(50),
    confirmed_at             TIMESTAMP,
    created_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at               TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Opportunity table
CREATE TABLE opportunity
(
    id          UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    title       VARCHAR(100) NOT NULL,
    description TEXT         NOT NULL,
    details     TEXT,
    type        VARCHAR(50)  NOT NULL CHECK (type IN ('JOB', 'VOLUNTEER', 'PARTNERSHIP')),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('OPEN', 'CLOSED')),
    visibility  VARCHAR(20)  NOT NULL CHECK (visibility IN ('PUBLIC', 'PRIVATE')),
    created_by  UUID         REFERENCES "user" (id) ON DELETE SET NULL,
    park_id     UUID         NOT NULL REFERENCES park (id) ON DELETE CASCADE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Opportunity Application table
CREATE TABLE opportunity_application
(
    id                     UUID PRIMARY KEY      DEFAULT uuid_generate_v4(),
    opportunity_id         UUID         NOT NULL REFERENCES opportunity (id) ON DELETE CASCADE,
    first_name             VARCHAR(50)  NOT NULL,
    last_name              VARCHAR(50)  NOT NULL,
    email                  VARCHAR(100) NOT NULL,
    application_letter_url VARCHAR(255) NOT NULL,
    approval_message       VARCHAR(1000),
    rejection_reason       VARCHAR(1000),
    status                 VARCHAR(20)  NOT NULL CHECK (status IN ('SUBMITTED', 'REVIEWED', 'ACCEPTED', 'REJECTED')),
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Audit Log table
CREATE TABLE audit_log
(
    id           UUID PRIMARY KEY     DEFAULT uuid_generate_v4(),
    action       VARCHAR(50) NOT NULL,
    entity_type  VARCHAR(50) NOT NULL,
    entity_id    UUID,
    details      TEXT,
    performed_by UUID        REFERENCES "user" (id) ON DELETE SET NULL,
    performed_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by  UUID        REFERENCES "user" (id) ON DELETE SET NULL,
    reviewed_at  TIMESTAMP
);

-- Triggers and Functions

-- Function to update income_stream actual_balance for donations
CREATE OR REPLACE FUNCTION update_income_stream_on_donation()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'CONFIRMED' AND (OLD.status IS NULL OR OLD.status != 'CONFIRMED') THEN
        UPDATE income_stream
        SET actual_balance = actual_balance + NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE budget_id = (
            SELECT id FROM budget
            WHERE park_id = NEW.park_id
              AND fiscal_year = NEW.fiscal_year
              AND status = 'APPROVED'
        )
          AND name = 'donation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER donation_income_stream_update
    AFTER INSERT OR UPDATE OF status
    ON donation
    FOR EACH ROW
EXECUTE FUNCTION update_income_stream_on_donation();

-- Function to update income_stream actual_balance for bookings
CREATE OR REPLACE FUNCTION update_income_stream_on_booking()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'CONFIRMED' AND (OLD.status IS NULL OR OLD.status != 'CONFIRMED') THEN
        UPDATE income_stream
        SET actual_balance = actual_balance + NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE budget_id = (
            SELECT id FROM budget
            WHERE park_id = NEW.park_id
              AND fiscal_year = EXTRACT(YEAR FROM NEW.created_at)
              AND status = 'APPROVED'
        )
          AND name = 'booking';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER booking_income_stream_update
    AFTER INSERT OR UPDATE OF status
    ON booking
    FOR EACH ROW
EXECUTE FUNCTION update_income_stream_on_booking();

-- Function to update budget balance as sum of budget_category balances
CREATE OR REPLACE FUNCTION sync_budget_balance()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE budget
    SET balance = COALESCE((
                               SELECT SUM(balance)
                               FROM budget_category
                               WHERE budget_id = NEW.budget_id
                           ), 0),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.budget_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER budget_category_balance_sync
    AFTER INSERT OR UPDATE OF balance
    ON budget_category
    FOR EACH ROW
EXECUTE FUNCTION sync_budget_balance();

-- Function to update unallocated amount on budget_category allocation changes
CREATE OR REPLACE FUNCTION update_budget_unallocated()
    RETURNS TRIGGER AS $$
DECLARE
    old_allocated DECIMAL(15,2);
    new_allocated DECIMAL(15,2);
BEGIN
    old_allocated := COALESCE(OLD.allocated_amount, 0);
    new_allocated := COALESCE(NEW.allocated_amount, 0);

    UPDATE budget
    SET unallocated = unallocated - (new_allocated - old_allocated),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.budget_id
      AND unallocated - (new_allocated - old_allocated) >= 0;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Insufficient unallocated funds for budget %', NEW.budget_id;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER budget_category_allocation
    AFTER INSERT OR UPDATE OF allocated_amount
    ON budget_category
    FOR EACH ROW
EXECUTE FUNCTION update_budget_unallocated();

-- Function to update budget_category and budget on funding request approval
CREATE OR REPLACE FUNCTION update_budget_balance_on_funding()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' AND NEW.approved_amount IS NOT NULL THEN
        UPDATE budget_category
        SET balance = balance + NEW.approved_amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.budget_category_id;

        UPDATE budget
        SET total_amount = total_amount + NEW.approved_amount,
            unallocated = unallocated + NEW.approved_amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.budget_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER funding_budget_balance
    AFTER INSERT OR UPDATE OF status, approved_amount
    ON funding_request
    FOR EACH ROW
EXECUTE FUNCTION update_budget_balance_on_funding();

-- Function to top up government income stream on budget approval
CREATE OR REPLACE FUNCTION top_up_government_income_stream()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' AND OLD.status != 'APPROVED' THEN
        UPDATE income_stream
        SET actual_balance = total_contribution,
            updated_at = CURRENT_TIMESTAMP
        WHERE budget_id = NEW.id
          AND name LIKE '%Government%';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER budget_government_approval
    AFTER UPDATE OF status
    ON budget
    FOR EACH ROW
EXECUTE FUNCTION top_up_government_income_stream();

-- Function to calculate budget_category allocated_amount
CREATE OR REPLACE FUNCTION calculate_category_allocation(budget_id UUID, percentage DECIMAL(5,2))
    RETURNS DECIMAL(15,2) AS $$
DECLARE
    budget_balance DECIMAL(15,2);
BEGIN
    SELECT balance INTO budget_balance
    FROM budget
    WHERE id = budget_id;

    RETURN budget_balance * (percentage / 100);
END;
$$ LANGUAGE plpgsql;

-- Trigger for expense affecting budget_category
CREATE OR REPLACE FUNCTION update_category_balance_on_expense()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE budget_category
    SET used_amount = used_amount + NEW.amount,
        balance = allocated_amount - (used_amount + NEW.amount),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.budget_category_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expense_category_balance
    AFTER INSERT
    ON expense
    FOR EACH ROW
EXECUTE FUNCTION update_category_balance_on_expense();

-- Trigger for expense affecting budget
CREATE OR REPLACE FUNCTION update_budget_balance_on_expense()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE budget
    SET balance = balance - NEW.amount,
        updated_at = CURRENT_TIMESTAMP
    WHERE id = NEW.budget_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expense_budget_balance
    AFTER INSERT
    ON expense
    FOR EACH ROW
EXECUTE FUNCTION update_budget_balance_on_expense();

-- Trigger for withdraw_request affecting budget_category
CREATE OR REPLACE FUNCTION update_category_balance_on_withdraw()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' THEN
        UPDATE budget_category
        SET used_amount = used_amount + NEW.amount,
            balance = allocated_amount - (used_amount + NEW.amount),
            updated_at = CURRENT_TIMESTAMP
        WHERE id = NEW.budget_category_id;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER withdraw_category_balance
    AFTER INSERT OR UPDATE OF status, amount
    ON withdraw_request
    FOR EACH ROW
EXECUTE FUNCTION update_category_balance_on_withdraw();

-- Trigger for withdraw_request affecting budget
CREATE OR REPLACE FUNCTION update_budget_balance_on_withdraw()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' THEN
        UPDATE budget
        SET balance = balance - NEW.amount,
            updated_at = CURRENT_TIMESTAMP
        WHERE id = (SELECT budget_id FROM budget_category WHERE id = NEW.budget_category_id);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER withdraw_budget_balance
    AFTER INSERT OR UPDATE OF status, amount
    ON withdraw_request
    FOR EACH ROW
EXECUTE FUNCTION update_budget_balance_on_withdraw();

-- Trigger to update updated_at timestamp for income_stream
CREATE OR REPLACE FUNCTION update_income_stream_timestamp()
    RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER income_stream_update_timestamp
    BEFORE UPDATE
    ON income_stream
    FOR EACH ROW
EXECUTE FUNCTION update_income_stream_timestamp();

-- -- Trigger to validate income_stream percentage and total_contribution
-- CREATE OR REPLACE FUNCTION validate_income_stream()
--     RETURNS TRIGGER AS $$
-- DECLARE
--     budget_total     DECIMAL(15, 2);
--     sum_percentage   DECIMAL(5, 2);
--     sum_contribution DECIMAL(15, 2);
-- BEGIN
--     -- Get budget total_amount
--     SELECT total_amount
--     INTO budget_total
--     FROM budget
--     WHERE id = NEW.budget_id;
-- 
--     -- Check if budget is in DRAFT status
--     IF (SELECT status FROM budget WHERE id = NEW.budget_id) != 'DRAFT' THEN
--         RAISE EXCEPTION 'Income streams can only be modified for DRAFT budgets';
--     END IF;
--
--     -- Calculate sum of percentages excluding the current row (for updates)
--     SELECT COALESCE(SUM(percentage), 0)
--     INTO sum_percentage
--     FROM income_stream
--     WHERE budget_id = NEW.budget_id
--       AND id != NEW.id;
--
--     -- Calculate sum of contributions excluding the current row
--     SELECT COALESCE(SUM(total_contribution), 0)
--     INTO sum_contribution
--     FROM income_stream
--     WHERE budget_id = NEW.budget_id
--       AND id != NEW.id;
--
--     -- Validate percentage
--     IF sum_percentage + NEW.percentage > 100 THEN
--         RAISE EXCEPTION 'Total percentage for budget % exceeds 100%%', NEW.budget_id;
--     END IF;
--
--     -- Validate total_contribution
--     IF sum_contribution + NEW.total_contribution > budget_total THEN
--         RAISE EXCEPTION 'Total contribution for budget % exceeds budget total amount', NEW.budget_id;
--     END IF;
--
--     RETURN NEW;
-- END;
-- $$ LANGUAGE plpgsql;
--
-- CREATE TRIGGER income_stream_validation
--     BEFORE INSERT OR UPDATE
--     ON income_stream
--     FOR EACH ROW
-- EXECUTE FUNCTION validate_income_stream();

-- Trigger to enforce GOVERNMENT_OFFICER approval for budgets
CREATE OR REPLACE FUNCTION check_government_approval()
    RETURNS TRIGGER AS $$
BEGIN
    IF NEW.status = 'APPROVED' AND NEW.approved_by IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM "user" WHERE id = NEW.approved_by AND role = 'GOVERNMENT_OFFICER') THEN
            RAISE EXCEPTION 'Only GOVERNMENT_OFFICER can approve budgets';
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER enforce_government_approval
    BEFORE UPDATE OF status
    ON budget
    FOR EACH ROW
EXECUTE FUNCTION check_government_approval();

-- Trigger to restore funds on expense deletion
CREATE OR REPLACE FUNCTION restore_expense_funds()
    RETURNS TRIGGER AS $$
BEGIN
    UPDATE budget_category
    SET used_amount = used_amount - OLD.amount,
        balance = balance + OLD.amount
    WHERE id = OLD.budget_category_id;

    UPDATE budget
    SET balance = balance + OLD.amount
    WHERE id = OLD.budget_id;

    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expense_delete_trigger
    AFTER DELETE
    ON expense
    FOR EACH ROW
EXECUTE FUNCTION restore_expense_funds();

-- Trigger to restore funds on withdraw_request deletion
CREATE OR REPLACE FUNCTION restore_withdraw_funds()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'APPROVED' THEN
        UPDATE budget_category
        SET used_amount = used_amount - OLD.amount,
            balance = balance + OLD.amount
        WHERE id = OLD.budget_category_id;

        UPDATE budget
        SET balance = balance + OLD.amount
        WHERE id = OLD.budget_id;
    END IF;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER withdraw_delete_trigger
    AFTER DELETE
    ON withdraw_request
    FOR EACH ROW
EXECUTE FUNCTION restore_withdraw_funds();

-- Trigger to log audit_status changes
CREATE OR REPLACE FUNCTION log_audit_status_change()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.audit_status != NEW.audit_status THEN
        INSERT INTO audit_log (action, entity_type, entity_id, details, performed_by, performed_at)
        VALUES ('UPDATE_AUDIT_STATUS',
                CASE
                    WHEN TG_TABLE_NAME = 'expense' THEN 'EXPENSE'
                    WHEN TG_TABLE_NAME = 'withdraw_request' THEN 'WITHDRAW_REQUEST'
                    END,
                NEW.id,
                'Changed audit_status from ' || OLD.audit_status || ' to ' || NEW.audit_status,
                NULL,
                CURRENT_TIMESTAMP);
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expense_audit_status_log
    AFTER UPDATE OF audit_status
    ON expense
    FOR EACH ROW
EXECUTE FUNCTION log_audit_status_change();

CREATE TRIGGER withdraw_audit_status_log
    AFTER UPDATE OF audit_status
    ON withdraw_request
    FOR EACH ROW
EXECUTE FUNCTION log_audit_status_change();

-- Trigger to restrict audit_status transitions
CREATE OR REPLACE FUNCTION restrict_audit_status_transition()
    RETURNS TRIGGER AS $$
BEGIN
    IF OLD.audit_status = 'UNJUSTIFIED' AND NEW.audit_status NOT IN ('PASSED', 'FAILED') THEN
        RAISE EXCEPTION 'audit_status can only transition from UNJUSTIFIED to PASSED or FAILED';
    END IF;
    IF OLD.audit_status IN ('PASSED', 'FAILED') AND NEW.audit_status = 'UNJUSTIFIED' THEN
        RAISE EXCEPTION 'Cannot revert audit_status to UNJUSTIFIED from PASSED or FAILED';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER expense_audit_status_transition
    BEFORE UPDATE OF audit_status
    ON expense
    FOR EACH ROW
EXECUTE FUNCTION restrict_audit_status_transition();

CREATE TRIGGER withdraw_audit_status_transition
    BEFORE UPDATE OF audit_status
    ON withdraw_request
    FOR EACH ROW
EXECUTE FUNCTION restrict_audit_status_transition();

-- Trigger to log budget category allocation changes
CREATE OR REPLACE FUNCTION log_category_allocation_change()
    RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO audit_log (action, entity_type, entity_id, details, performed_by, performed_at)
    VALUES ('UPDATE_ALLOCATION', 'BUDGET_CATEGORY', NEW.id,
            'Changed allocated_amount from ' || OLD.allocated_amount || ' to ' || NEW.allocated_amount,
            NULL, CURRENT_TIMESTAMP);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER budget_category_allocation_log
    AFTER UPDATE OF allocated_amount
    ON budget_category
    FOR EACH ROW
EXECUTE FUNCTION log_category_allocation_change();

-- Indexes for Performance
CREATE INDEX idx_user_email ON "user" (email);
CREATE INDEX idx_budget_park ON budget (park_id);
CREATE INDEX idx_expense_park ON expense (park_id);
CREATE INDEX idx_expense_category ON expense (budget_category_id);
CREATE INDEX idx_expense_budget ON expense (budget_id);
CREATE INDEX idx_expense_audit_status ON expense (audit_status);
CREATE INDEX idx_withdraw_request_status ON withdraw_request (status);
CREATE INDEX idx_withdraw_request_audit_status ON withdraw_request (audit_status);
CREATE INDEX idx_booking_visitor ON booking (visitor_id);
CREATE INDEX idx_booking_park ON booking (park_id);
CREATE INDEX idx_donation_donor ON donation (donor_id);
CREATE INDEX idx_donation_park ON donation (park_id);
CREATE INDEX idx_audit_log_performed_by ON audit_log (performed_by);
CREATE INDEX idx_audit_log_reviewed_by ON audit_log (reviewed_by);
CREATE INDEX idx_funding_request_park ON funding_request (park_id);
CREATE INDEX idx_funding_request_status ON funding_request (status);
CREATE INDEX idx_activity_park ON activity (park_id);
CREATE INDEX idx_booking_activity ON booking (activity_id);
CREATE INDEX idx_user_park ON "user" (park_id);
CREATE INDEX idx_income_stream_budget ON income_stream (budget_id);
CREATE INDEX idx_income_stream_park ON income_stream (park_id);
CREATE INDEX idx_income_stream_fiscal_year ON income_stream (fiscal_year);
CREATE INDEX idx_funding_request_category ON funding_request (budget_category_id);

-- Views
CREATE VIEW park_financial_summary AS
SELECT p.id                             AS park_id,
       p.name                           AS park_name,
       COALESCE(SUM(b.total_amount), 0) AS total_budget,
       COALESCE(SUM(b.balance), 0)      AS remaining_budget,
       COALESCE(SUM(d.amount), 0)       AS total_donations,
       COALESCE(SUM(bk.amount), 0)      AS total_booking_revenue,
       COALESCE(SUM(e.amount), 0)       AS total_expenses,
       COALESCE(SUM(wr.amount), 0)      AS total_withdrawals
FROM park p
         LEFT JOIN budget b ON p.id = b.park_id AND b.status = 'APPROVED'
         LEFT JOIN donation d ON p.id = d.park_id AND d.status = 'CONFIRMED'
         LEFT JOIN booking bk ON p.id = bk.park_id AND bk.status = 'CONFIRMED'
         LEFT JOIN expense e ON p.id = e.park_id AND e.audit_status = 'PASSED'
         LEFT JOIN withdraw_request wr ON p.id = wr.park_id AND wr.status = 'APPROVED' AND wr.audit_status = 'PASSED'
GROUP BY p.id, p.name;

CREATE VIEW budget_utilization AS
SELECT b.id                                                                       AS budget_id,
       p.name                                                                     AS park_name,
       b.fiscal_year,
       b.total_amount,
       b.balance,
       b.unallocated,
       (b.total_amount - b.balance)                                               AS utilized_amount,
       ROUND(((b.total_amount - b.balance) / NULLIF(b.total_amount, 0)) * 100, 2) AS utilization_percentage,
       COUNT(DISTINCT bc.id)                                                      AS category_count,
       COALESCE(SUM(e.amount), 0)                                                 AS total_expenses,
       COALESCE(SUM(wr.amount), 0)                                                AS total_withdrawals
FROM budget b
         JOIN park p ON b.park_id = p.id
         LEFT JOIN budget_category bc ON b.id = bc.budget_id
         LEFT JOIN expense e ON bc.id = e.budget_category_id AND e.audit_status = 'PASSED'
         LEFT JOIN withdraw_request wr
                   ON bc.id = wr.budget_category_id AND wr.status = 'APPROVED' AND wr.audit_status = 'PASSED'
GROUP BY b.id, p.name, b.fiscal_year, b.total_amount, b.balance, b.unallocated;

CREATE VIEW audit_status_summary AS
SELECT 'expense' AS entity_type,
       audit_status,
       COUNT(*)  AS count
FROM expense
GROUP BY audit_status
UNION ALL
SELECT 'withdraw_request' AS entity_type,
       audit_status,
       COUNT(*)           AS count
FROM withdraw_request
GROUP BY audit_status;

CREATE VIEW donation_by_fiscal_year AS
SELECT d.id,
       d.donor_id,
       d.park_id,
       d.amount,
       d.status,
       d.payment_reference,
       d.currency,
       d.fiscal_year,
       d.confirmed_at,
       d.created_at,
       d.updated_at,
       b.id AS budget_id
FROM donation d
         JOIN budget b ON d.park_id = b.park_id AND d.fiscal_year = b.fiscal_year;
```