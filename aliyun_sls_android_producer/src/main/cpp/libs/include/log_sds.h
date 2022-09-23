/* SDSLib, A C dynamic strings library
 *
 * Copyright (c) 2006-2010, Salvatore Sanfilippo <antirez at gmail dot com>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *   * Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   * Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   * Neither the name of Redis nor the names of its contributors may be used
 *     to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef __LOG_SDS_H
#define __LOG_SDS_H

#define LOG_SDS_MAX_PREALLOC (1024*1024)

#include <sys/types.h>
#include <stdarg.h>

#ifdef WIN32
#define inline __inline
#endif

typedef char *log_sds;

struct log_sdshdr {
    unsigned int len;
    unsigned int free;
    char buf[];
};

size_t log_sdslen(const log_sds s);

size_t log_sdsavail(const log_sds s);

log_sds log_sdsnewlen(const void *init, size_t initlen);

log_sds log_sdsnewEmpty(size_t preAlloclen);

log_sds log_sdsnew(const char *init);

log_sds log_sdsempty(void);

size_t log_sdslen(const log_sds s);

log_sds log_sdsdup(const log_sds s);

void log_sdsfree(log_sds s);

size_t log_sdsavail(const log_sds s);

log_sds log_sdsgrowzero(log_sds s, size_t len);

log_sds log_sdscatlen(log_sds s, const void *t, size_t len);

log_sds log_sdscat(log_sds s, const char *t);

log_sds log_sdscatchar(log_sds s, char c);

log_sds log_sdscatsds(log_sds s, const log_sds t);

log_sds log_sdscpylen(log_sds s, const char *t, size_t len);

log_sds log_sdscpy(log_sds s, const char *t);

log_sds log_sdscatvprintf(log_sds s, const char *fmt, va_list ap);

log_sds log_sdscatrepr(log_sds s, const char *p, size_t len);

#ifdef __GNUC__

log_sds log_sdscatprintf(log_sds s, const char *fmt, ...)
__attribute__((format(printf, 2, 3)));

#else
log_sds log_sdscatprintf(log_sds s, const char *fmt, ...);
#endif


#endif
