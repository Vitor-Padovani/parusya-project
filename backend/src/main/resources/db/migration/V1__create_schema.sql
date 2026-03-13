-- =========================
-- TABELAS BASE (SEM FKs)
-- =========================

CREATE TABLE groups (
                        id uuid PRIMARY KEY,
                        created_at timestamp(6) NOT NULL,
                        name varchar(255) NOT NULL
);

CREATE TABLE participants (
                              id uuid PRIMARY KEY,
                              birth_date date NOT NULL,
                              created_at timestamp(6) NOT NULL,
                              email varchar(255) NOT NULL UNIQUE,
                              full_name varchar(255) NOT NULL,
                              gender varchar(255) NOT NULL CHECK (gender IN ('MALE','FEMALE','OTHER')),
                              password varchar(255) NOT NULL,
                              phone varchar(255) NOT NULL UNIQUE
);

CREATE TABLE organizers (
                            id uuid PRIMARY KEY,
                            created_at timestamp(6) NOT NULL,
                            group_id uuid NOT NULL,
                            email varchar(255) NOT NULL UNIQUE,
                            name varchar(255) NOT NULL,
                            password varchar(255) NOT NULL
);

CREATE TABLE tags (
                      id uuid PRIMARY KEY,
                      group_id uuid NOT NULL,
                      name varchar(255) NOT NULL,
                      CONSTRAINT uk_tag_name_group UNIQUE (name, group_id)
);

CREATE TABLE events (
                        id uuid PRIMARY KEY,
                        is_active boolean NOT NULL,
                        created_at timestamp(6) NOT NULL,
                        start_date_time timestamp(6) NOT NULL,
                        group_id uuid NOT NULL,
                        description TEXT,
                        name varchar(255) NOT NULL
);

CREATE TABLE event_staff (
                             id uuid PRIMARY KEY,
                             created_at timestamp(6) NOT NULL,
                             created_by uuid,
                             group_id uuid NOT NULL,
                             email varchar(255) NOT NULL UNIQUE,
                             name varchar(255) NOT NULL,
                             password varchar(255) NOT NULL
);

CREATE TABLE qr_codes (
                          id uuid PRIMARY KEY,
                          created_at timestamp(6) NOT NULL,
                          event_id uuid,
                          participant_id uuid NOT NULL,
                          encoded_data varchar(255) NOT NULL UNIQUE
);

CREATE TABLE check_ins (
                           id uuid PRIMARY KEY,
                           timestamp timestamp(6) NOT NULL,
                           event_id uuid NOT NULL,
                           participant_id uuid NOT NULL,
                           staff_id uuid,
                           CONSTRAINT uk_checkin_participant_event UNIQUE (participant_id, event_id)
);

CREATE TABLE event_tags (
                            event_id uuid NOT NULL,
                            tag_id uuid NOT NULL
);

-- =========================
-- ÍNDICES
-- =========================

CREATE INDEX idx_checkin_event_timestamp
    ON check_ins (event_id, timestamp);

CREATE INDEX idx_events_group_active
    ON events (group_id, is_active);

-- =========================
-- FOREIGN KEYS
-- =========================

ALTER TABLE organizers
    ADD CONSTRAINT fk_organizers_group
        FOREIGN KEY (group_id) REFERENCES groups;

ALTER TABLE tags
    ADD CONSTRAINT fk_tags_group
        FOREIGN KEY (group_id) REFERENCES groups;

ALTER TABLE events
    ADD CONSTRAINT fk_events_group
        FOREIGN KEY (group_id) REFERENCES groups;

ALTER TABLE event_staff
    ADD CONSTRAINT fk_event_staff_group
        FOREIGN KEY (group_id) REFERENCES groups;

ALTER TABLE event_staff
    ADD CONSTRAINT fk_event_staff_created_by
        FOREIGN KEY (created_by) REFERENCES organizers;

ALTER TABLE qr_codes
    ADD CONSTRAINT fk_qr_event
        FOREIGN KEY (event_id) REFERENCES events;

ALTER TABLE qr_codes
    ADD CONSTRAINT fk_qr_participant
        FOREIGN KEY (participant_id) REFERENCES participants;

ALTER TABLE check_ins
    ADD CONSTRAINT fk_checkin_event
        FOREIGN KEY (event_id) REFERENCES events;

ALTER TABLE check_ins
    ADD CONSTRAINT fk_checkin_participant
        FOREIGN KEY (participant_id) REFERENCES participants;

ALTER TABLE check_ins
    ADD CONSTRAINT fk_checkin_staff
        FOREIGN KEY (staff_id) REFERENCES event_staff;

ALTER TABLE event_tags
    ADD CONSTRAINT fk_event_tags_event
        FOREIGN KEY (event_id) REFERENCES events;

ALTER TABLE event_tags
    ADD CONSTRAINT fk_event_tags_tag
        FOREIGN KEY (tag_id) REFERENCES tags;