#ifndef LOG_MD5_H_
#define LOG_MD5_H_

#include <stddef.h>
#include <stdint.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * \brief          MD5 context structure
 */
typedef struct
{
    uint32_t total[2];          /*!< number of bytes processed  */
    uint32_t state[4];          /*!< intermediate digest state  */
    unsigned char buffer[64];   /*!< data block being processed */
} log_mbedtls_md5_context;

/**
 * \brief          Initialize MD5 context
 *
 * \param ctx      MD5 context to be initialized
 */
void log_mbedtls_md5_init( log_mbedtls_md5_context *ctx );


/**
 * \brief          Clone (the state of) an MD5 context
 *
 * \param dst      The destination context
 * \param src      The context to be cloned
 */
void log_mbedtls_md5_clone( log_mbedtls_md5_context *dst,
                        const log_mbedtls_md5_context *src );

/**
 * \brief          MD5 context setup
 *
 * \param ctx      context to be initialized
 */
void log_mbedtls_md5_starts( log_mbedtls_md5_context *ctx );

/**
 * \brief          MD5 process buffer
 *
 * \param ctx      MD5 context
 * \param input    buffer holding the  data
 * \param ilen     length of the input data
 */
void log_mbedtls_md5_update( log_mbedtls_md5_context *ctx, const unsigned char *input, size_t ilen );

/**
 * \brief          MD5 final digest
 *
 * \param ctx      MD5 context
 * \param output   MD5 checksum result
 */
void log_mbedtls_md5_finish( log_mbedtls_md5_context *ctx, unsigned char output[16] );

/* Internal use */
void log_mbedtls_md5_process( log_mbedtls_md5_context *ctx, const unsigned char data[64] );


/**
 * \brief          Output = MD5( input buffer )
 *
 * \param input    buffer holding the  data
 * \param ilen     length of the input data
 * \param output   MD5 checksum result
 */
void log_mbedtls_md5( const unsigned char *input, size_t ilen, unsigned char output[16] );


#ifdef __cplusplus
}
#endif


#endif /*LOG_MD5_H_*/
