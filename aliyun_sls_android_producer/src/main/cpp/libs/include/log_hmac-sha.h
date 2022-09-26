

#ifndef LOG_HMACSHA1_H_
#define LOG_HMACSHA1_H_

#include "log_sha1.h"

#define LOG_HMAC_SHA1_BITS        LOG_SHA1_HASH_BITS
#define LOG_HMAC_SHA1_BYTES       LOG_SHA1_HASH_BYTES
#define LOG_HMAC_SHA1_BLOCK_BITS  LOG_SHA1_BLOCK_BITS
#define LOG_HMAC_SHA1_BLOCK_BYTES LOG_SHA1_BLOCK_BYTES

typedef struct{
    log_sha1_ctx_t a, b;
} log_hmac_sha1_ctx_t;


void log_hmac_sha1_init(log_hmac_sha1_ctx_t *s, const void *key, uint16_t keylength_b);
void log_hmac_sha1_nextBlock(log_hmac_sha1_ctx_t *s, const void *block);
void log_hmac_sha1_lastBlock(log_hmac_sha1_ctx_t *s, const void *block, uint16_t length_b);
void log_hmac_sha1_final(void *dest, log_hmac_sha1_ctx_t *s);

void log_hmac_sha1(void *dest, const void *key, uint16_t keylength_b, const void *msg, uint32_t msglength_b);

#endif /*LOG_HMACSHA1_H_*/
