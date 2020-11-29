CREATE TABLE fire_drill_permission (
    id UUID PRIMARY KEY,
    channel_id BIGINT,
    role_id BIGINT,
    allow_permission BIGINT,
    deny_permission BIGINT,
    inherit_permission BIGINT
);
