package com.smartling.api.sdk.exceptions;

import java.util.List;

public class OperationsLimitExceeded extends ApiException
{
    OperationsLimitExceeded(final String contents, final List<String> messages, final int httpCode)
    {
        super(contents, messages, httpCode);
    }
}
