package com.smartling.api.sdk.file;

import com.smartling.api.sdk.ConnectionConfig;
import com.smartling.api.sdk.ProxyConfiguration;
import com.smartling.api.sdk.auth.AuthenticationContext;
import com.smartling.api.sdk.dto.file.FileLastModified;
import com.smartling.api.sdk.dto.file.StringResponse;
import com.smartling.api.sdk.dto.file.UploadFileData;
import com.smartling.api.sdk.exceptions.ApiException;
import com.smartling.api.sdk.file.parameters.FileImportParameterBuilder;
import com.smartling.api.sdk.file.parameters.FileLastModifiedParameterBuilder;
import com.smartling.api.sdk.file.parameters.FileListSearchParameter;
import com.smartling.api.sdk.file.parameters.FileUploadParameterBuilder;
import com.smartling.api.sdk.file.parameters.GetFileParameterBuilder;
import com.smartling.api.sdk.file.parameters.GetOriginalFileParameterBuilder;
import com.smartling.api.sdk.file.response.AuthorizedLocales;
import com.smartling.api.sdk.file.response.EmptyResponse;
import com.smartling.api.sdk.file.response.FileImportData;
import com.smartling.api.sdk.file.response.FileList;
import com.smartling.api.sdk.file.response.FileLocaleStatus;
import com.smartling.api.sdk.file.response.FileStatus;
import com.smartling.api.sdk.file.response.Response;
import com.smartling.api.sdk.util.HttpUtils;
import com.smartling.web.api.v2.ResponseCode;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.InputStream;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FileApiClientTest
{
    private static final String CALLBACK_URL = "callbackUrl";
    private static final String BASE_PATH = "https://api.smartling.com";
    private static final String PROJECT_ID = "testProject";
    private static final String LOCALE = "en-US";
    private static final String FILE_URI = "fileUri";
    private static final String FILE_URI2 = "fileUri2";
    private static final String ENCODING = "UTF-8";
    private static final String USER_TOKEN = "UserToken";

    private AuthenticationContext authenticationContext;
    private FileApiClient fileApiClient;
    private HttpUtils httpUtils;
    private StringResponse response;
    private ArgumentCaptor<HttpRequestBase> requestCaptor = ArgumentCaptor.forClass(HttpRequestBase.class);
    private ProxyConfiguration proxyConfiguration;
    private ConnectionConfig connectionConfig;

    @Before
    public void setup() throws ApiException
    {
        proxyConfiguration = mock(ProxyConfiguration.class);
        authenticationContext = mock(AuthenticationContext.class);
        fileApiClient = new FileApiClient();
        httpUtils = mock(HttpUtils.class);
        fileApiClient.setHttpUtils(httpUtils);
        response = mock(StringResponse.class);
        connectionConfig = new ConnectionConfig(proxyConfiguration, BASE_PATH, PROJECT_ID);
        when(authenticationContext.getAuthorizationTokenString()).thenReturn(USER_TOKEN);
        doCallRealMethod().when(authenticationContext).applyTo(any(HttpMessage.class));
        when(response.isSuccess()).thenReturn(true);
        when(httpUtils.executeHttpCall(requestCaptor.capture(), eq(proxyConfiguration))).thenReturn(response);
    }

    @Test
    public void testUploadFile() throws Exception
    {
        FileUploadParameterBuilder fileUploadParameterBuilder = getFileUploadParameterBuilder();
        File fileToUpload = mock(File.class);
        when(response.getContents()).thenReturn(ResponseExamples.UPLOAD_RESPONSE);
        Response<UploadFileData> uploadFileDataResponse = fileApiClient.uploadFile(authenticationContext, fileToUpload, ENCODING, fileUploadParameterBuilder, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertNotNull(((HttpPost)request).getEntity());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, uploadFileDataResponse.getCode());
        assertEquals(false, uploadFileDataResponse.getData().isOverWritten());
        assertEquals(1, uploadFileDataResponse.getData().getStringCount());
        assertEquals(2, uploadFileDataResponse.getData().getWordCount());
    }

    @Test
    public void testUploadFileFromStream() throws Exception
    {
        FileUploadParameterBuilder fileUploadParameterBuilder = getFileUploadParameterBuilder();
        InputStream inputStream = mock(InputStream.class);
        when(response.getContents()).thenReturn(ResponseExamples.UPLOAD_RESPONSE);
        fileApiClient.uploadFile(authenticationContext, inputStream, FILE_URI, ENCODING, fileUploadParameterBuilder, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertNotNull(((HttpPost)request).getEntity());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file", request.getURI().toString());
    }

    @Test
    public void testDeleteFile() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.EMPTY_RESPONSE);
        Response<EmptyResponse> apiResponse = fileApiClient.deleteFile(authenticationContext, FILE_URI, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/delete", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(EmptyResponse.class, apiResponse.getData().getClass());
    }

    @Test
    public void testRenameFile() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.EMPTY_RESPONSE);
        Response<EmptyResponse> apiResponse = fileApiClient.renameFile(authenticationContext, FILE_URI, FILE_URI2, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/rename", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(EmptyResponse.class, apiResponse.getData().getClass());
    }

    @Test
    public void testGetLastModified() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.LAST_MODIFICATION_RESPONSE);
        Response<FileLastModified> apiResponse = fileApiClient.getLastModified(authenticationContext, new FileLastModifiedParameterBuilder().fileUri(FILE_URI), connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpGet.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/last_modified?fileUri=fileUri", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(5, apiResponse.getData().getItems().size());
        assertEquals(5, apiResponse.getData().getTotalCount());
        assertEquals("be-BY", apiResponse.getData().getItems().get(0).getLocaleId());
        assertEquals("Wed Sep 16 00:24:42 EEST 2015", apiResponse.getData().getItems().get(0).getLastModified().toString());
    }

    @Test
    public void testGetFile() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.GET_FILE_RESPONSE);
        fileApiClient.getFile(authenticationContext, LOCALE, new GetFileParameterBuilder().fileUri(FILE_URI), connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/locales/" + LOCALE + "/file?fileUri=fileUri", request.getURI().toString());
    }

    @Test
    public void testGetOriginalFile() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.GET_FILE_RESPONSE);
        fileApiClient.getOriginalFile(authenticationContext, new GetOriginalFileParameterBuilder().fileUri(FILE_URI), connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file?fileUri=fileUri", request.getURI().toString());
    }

    @Test
    public void testGetFilesList() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.FILE_LIST_RESPONSE);
        Response<FileList> apiResponse = fileApiClient.getFilesList(authenticationContext, new FileListSearchParameter(), connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpGet.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/files/list?", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(2, apiResponse.getData().getItems().size());
        assertEquals(2, apiResponse.getData().getTotalCount());
        assertEquals("FileListItem[fileUri=3-namespace-explicit.xml,lastUploaded=2015-07-29T10:34:30+0000,fileType=xml]", apiResponse.getData().getItems().get(0).toString());
    }

    @Test
    public void testGetFileLocaleStatus() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.FILE_LOCALES_STATUS_RESPONSE);
        Response<FileLocaleStatus> apiResponse = fileApiClient.getFileLocaleStatus(authenticationContext, LOCALE, FILE_URI, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpGet.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/locales/fileUri/file/status?fileUri=en-US", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(
                "FileLocaleStatus[fileUri=test-cancel-translation.xml,totalStringCount=1,totalWordCount=3,authorizedStringCount=0,authorizedWordCount=0,completedStringCount=0,"
                        + "completedWordCount=0,excludedStringCount=0,excludedWordCount=0,lastUploaded=2015-09-15T21:28:25+0000,fileType=xml]",
                apiResponse.getData().toString()
        );
    }

    @Test
    public void testGetFileStatus() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.FILE_STATUS_RESPONSE);
        Response<FileStatus> apiResponse = fileApiClient.getFileStatus(authenticationContext, FILE_URI, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpGet.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/status?fileUri=fileUri", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(5, apiResponse.getData().getItems().size());
        assertEquals(5, apiResponse.getData().getTotalCount());
        assertEquals(3, apiResponse.getData().getTotalWordCount());
        assertEquals(1, apiResponse.getData().getTotalStringCount());
        assertEquals(
                "FileStatusItem[localeId=be-BY,authorizedStringCount=0,authorizedWordCount=0,completedStringCount=0,completedWordCount=0,excludedStringCount=0,"
                        + "excludedWordCount=0]",
                apiResponse.getData().getItems().get(0).toString()
        );
    }

    @Test
    public void testImportTranslations() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.IMPORT_TRANSLATIONS_RESPONSE);
        File fileToImport = mock(File.class);
        Response<FileImportData> apiResponse = fileApiClient
                .importTranslations(authenticationContext, fileToImport, LOCALE, ENCODING, new FileImportParameterBuilder().fileType(FileType.CSV), connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/locales/en-US/file/import", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(0, apiResponse.getData().getStringCount());
        assertEquals(0, apiResponse.getData().getTranslationImportErrors().size());
    }

    @Test
    public void testGetAuthorizedLocales() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.FILE_AUTHORIZED_LOCALES_RESPONSE);
        Response<AuthorizedLocales> apiResponse = fileApiClient.getAuthorizedLocales(authenticationContext, FILE_URI, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpGet.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/authorized_locales?fileUri=fileUri", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(2, apiResponse.getData().getItems().size());
        assertEquals("it-IT", apiResponse.getData().getItems().get(0));
    }

    @Test
    public void testAuthorizeLocales() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.EMPTY_RESPONSE);
        Response<EmptyResponse> apiResponse = fileApiClient.authorizeLocales(authenticationContext, FILE_URI, new String[]{LOCALE}, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpPost.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/authorized_locales", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(EmptyResponse.class, apiResponse.getData().getClass());
    }

    @Test
    public void testUnAuthorizeLocales() throws Exception
    {
        when(response.getContents()).thenReturn(ResponseExamples.EMPTY_RESPONSE);
        Response<EmptyResponse> apiResponse = fileApiClient.unAuthorizeLocales(authenticationContext, FILE_URI, new String[]{LOCALE}, connectionConfig);
        HttpRequestBase request = requestCaptor.getValue();
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(USER_TOKEN, request.getFirstHeader(HttpHeaders.AUTHORIZATION).getValue());
        assertEquals(HttpDelete.class, request.getClass());
        assertEquals("https://api.smartling.com/files-api/v2/projects/testProject/file/authorized_locales?fileUri=fileUri&localeIds%5B%5D=en-US", request.getURI().toString());
        assertEquals(ResponseCode.SUCCESS, apiResponse.getCode());
        assertEquals(EmptyResponse.class, apiResponse.getData().getClass());
    }

    private FileUploadParameterBuilder getFileUploadParameterBuilder()
    {
        FileUploadParameterBuilder fileUploadParameterBuilder = new FileUploadParameterBuilder();
        fileUploadParameterBuilder.approveContent(true);
        fileUploadParameterBuilder.callbackUrl(CALLBACK_URL);
        fileUploadParameterBuilder.fileType(FileType.JAVA_PROPERTIES);
        fileUploadParameterBuilder.localesToApprove(Collections.singletonList(LOCALE));
        fileUploadParameterBuilder.overwriteApprovedLocales(true);
        fileUploadParameterBuilder.fileUri(FILE_URI);
        return fileUploadParameterBuilder;
    }
}