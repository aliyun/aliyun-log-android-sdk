package com.aliyun.sls.android.ot

import kotlinx.coroutines.CoroutineScope

/**
 * @author gordon
 * @date 2022/10/27
 */
class CoroutineScopeSpan(
    private val scope: CoroutineScope,
    private val span: Span
) : CoroutineScope by scope