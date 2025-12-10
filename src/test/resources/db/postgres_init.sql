CREATE SEQUENCE IF NOT EXISTS user_account_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE SEQUENCE IF NOT EXISTS catalog_items_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

CREATE TABLE IF NOT EXISTS user_accounts
(
    id bigint NOT NULL,
    login character varying(255) COLLATE pg_catalog."default" NOT NULL,
    password character varying(255) COLLATE pg_catalog."default" NOT NULL,
    access character varying(255) COLLATE pg_catalog."default",
    CONSTRAINT user_accounts_pkey PRIMARY KEY (id),
    CONSTRAINT user_accounts_login_key UNIQUE (login)
)
TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS user_accounts_login
    ON user_accounts USING btree
    (login COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;

CREATE TABLE IF NOT EXISTS catalog_items
(
    id bigint NOT NULL,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    category character varying(255) COLLATE pg_catalog."default" NOT NULL,
    brand character varying(255) COLLATE pg_catalog."default" NOT NULL,
    price double precision,
    CONSTRAINT catalog_items_pkey PRIMARY KEY (id)
)
TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS catalog_items_brand
    ON catalog_items USING btree
    (brand COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS catalog_items_category
    ON catalog_items USING btree
    (category COLLATE pg_catalog."default" ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;

CREATE INDEX IF NOT EXISTS catalog_items_price
    ON catalog_items USING btree
    (price ASC NULLS LAST)
    WITH (fillfactor=100, deduplicate_items=True)
    TABLESPACE pg_default;

INSERT INTO user_accounts(id, login, password, access)
	VALUES (nextval('user_account_seq'), 'root', '$2a$10$2qRE6fQGuhuaaqqn.kYjKOH5K6NfQV5lVp5bbsyMEkWu6oqR3BwkG', 'FULL');

INSERT INTO user_accounts(id, login, password, access)
	VALUES (nextval('user_account_seq'), 'user', '$2a$10$7TaEjk7cpdbtqTPNmg/vpeRGzw3jUr37OpCZbriOhaXDJw3vpWzG6', 'READ');

INSERT INTO catalog_items(id, name, category, brand, price)
	VALUES (nextval('catalog_items_seq'), 'Blue dress', 'Dress', 'Zolla', 101.89);

INSERT INTO catalog_items(id, name, category, brand, price)
	VALUES (nextval('catalog_items_seq'), 'Wide-brimmed hat', 'Hat', 'HM', 15.5);

INSERT INTO catalog_items(id, name, category, brand, price)
	VALUES (nextval('catalog_items_seq'), 'Cocktail dress', 'Dress', 'HM', 250);