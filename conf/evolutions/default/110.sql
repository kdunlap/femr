# --- !Ups

ALTER TABLE `users`
    ADD COLUMN `refresh_token` TEXT NULL DEFAULT NULL AFTER `password`,
    ADD COLUMN `refresh_token_issued_at` DATETIME NULL DEFAULT NULL AFTER `refresh_token`;

# --- !Downs

ALTER TABLE `users`
  DROP COLUMN `refresh_token`,
  DROP COLUMN `refresh_token_issued_at`;