ALTER TABLE hg_pronoun
    ADD CONSTRAINT hg_pronoun_unique UNIQUE (subject, object, possessive, self);
