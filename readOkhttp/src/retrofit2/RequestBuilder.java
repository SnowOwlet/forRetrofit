package retrofit2;

import java.io.IOException;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;

final class RequestBuilder {
  private static final char[] HEX_DIGITS = new char[] { 
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
      'A', 'B', 'C', 'D', 'E', 'F' };
  
  private static final String PATH_SEGMENT_ALWAYS_ENCODE_SET = " \"<>^`{}|\\?#";
  
  private static final Pattern PATH_TRAVERSAL = Pattern.compile("(.*/)?(\\.|%2e|%2E){1,2}(/.*)?");
  
  private final String method;
  
  private final HttpUrl baseUrl;
  
  @Nullable
  private String relativeUrl;
  
  @Nullable
  private HttpUrl.Builder urlBuilder;
  
  private final Request.Builder requestBuilder;
  
  private final Headers.Builder headersBuilder;
  
  @Nullable
  private MediaType contentType;
  
  private final boolean hasBody;
  
  @Nullable
  private MultipartBody.Builder multipartBuilder;
  
  @Nullable
  private FormBody.Builder formBuilder;
  
  @Nullable
  private RequestBody body;
  
  RequestBuilder(String method, HttpUrl baseUrl, @Nullable String relativeUrl, @Nullable Headers headers, @Nullable MediaType contentType, boolean hasBody, boolean isFormEncoded, boolean isMultipart) {
    this.method = method;
    this.baseUrl = baseUrl;
    this.relativeUrl = relativeUrl;
    this.requestBuilder = new Request.Builder();
    this.contentType = contentType;
    this.hasBody = hasBody;
    if (headers != null) {
      this.headersBuilder = headers.newBuilder();
    } else {
      this.headersBuilder = new Headers.Builder();
    } 
    if (isFormEncoded) {
      this.formBuilder = new FormBody.Builder();
    } else if (isMultipart) {
      this.multipartBuilder = new MultipartBody.Builder();
      this.multipartBuilder.setType(MultipartBody.FORM);
    } 
  }
  
  void setRelativeUrl(Object relativeUrl) {
    this.relativeUrl = relativeUrl.toString();
  }
  
  void addHeader(String name, String value) {
    if ("Content-Type".equalsIgnoreCase(name)) {
      try {
        this.contentType = MediaType.get(value);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Malformed content type: " + value, e);
      } 
    } else {
      this.headersBuilder.add(name, value);
    } 
  }
  
  void addHeaders(Headers headers) {
    this.headersBuilder.addAll(headers);
  }
  
  void addPathParam(String name, String value, boolean encoded) {
    if (this.relativeUrl == null)
      throw new AssertionError(); 
    String replacement = canonicalizeForPath(value, encoded);
    String newRelativeUrl = this.relativeUrl.replace("{" + name + "}", replacement);
    if (PATH_TRAVERSAL.matcher(newRelativeUrl).matches())
      throw new IllegalArgumentException("@Path parameters shouldn't perform path traversal ('.' or '..'): " + value); 
    this.relativeUrl = newRelativeUrl;
  }
  
  private static String canonicalizeForPath(String input, boolean alreadyEncoded) {
    for (int i = 0, limit = input.length(); i < limit; i += Character.charCount(codePoint)) {
      int codePoint = input.codePointAt(i);
      if (codePoint < 32 || codePoint >= 127 || " \"<>^`{}|\\?#"
        
        .indexOf(codePoint) != -1 || (!alreadyEncoded && (codePoint == 47 || codePoint == 37))) {
        Buffer out = new Buffer();
        out.writeUtf8(input, 0, i);
        canonicalizeForPath(out, input, i, limit, alreadyEncoded);
        return out.readUtf8();
      } 
    } 
    return input;
  }
  
  private static void canonicalizeForPath(Buffer out, String input, int pos, int limit, boolean alreadyEncoded) {
    Buffer utf8Buffer = null;
    int i;
    for (i = pos; i < limit; i += Character.charCount(codePoint)) {
      int codePoint = input.codePointAt(i);
      if (!alreadyEncoded || (codePoint != 9 && codePoint != 10 && codePoint != 12 && codePoint != 13))
        if (codePoint < 32 || codePoint >= 127 || " \"<>^`{}|\\?#"
          
          .indexOf(codePoint) != -1 || (!alreadyEncoded && (codePoint == 47 || codePoint == 37))) {
          if (utf8Buffer == null)
            utf8Buffer = new Buffer(); 
          utf8Buffer.writeUtf8CodePoint(codePoint);
          while (!utf8Buffer.exhausted()) {
            int b = utf8Buffer.readByte() & 0xFF;
            out.writeByte(37);
            out.writeByte(HEX_DIGITS[b >> 4 & 0xF]);
            out.writeByte(HEX_DIGITS[b & 0xF]);
          } 
        } else {
          out.writeUtf8CodePoint(codePoint);
        }  
    } 
  }
  
  void addQueryParam(String name, @Nullable String value, boolean encoded) {
    if (this.relativeUrl != null) {
      this.urlBuilder = this.baseUrl.newBuilder(this.relativeUrl);
      if (this.urlBuilder == null)
        throw new IllegalArgumentException("Malformed URL. Base: " + this.baseUrl + ", Relative: " + this.relativeUrl); 
      this.relativeUrl = null;
    } 
    if (encoded) {
      this.urlBuilder.addEncodedQueryParameter(name, value);
    } else {
      this.urlBuilder.addQueryParameter(name, value);
    } 
  }
  
  void addFormField(String name, String value, boolean encoded) {
    if (encoded) {
      this.formBuilder.addEncoded(name, value);
    } else {
      this.formBuilder.add(name, value);
    } 
  }
  
  void addPart(Headers headers, RequestBody body) {
    this.multipartBuilder.addPart(headers, body);
  }
  
  void addPart(MultipartBody.Part part) {
    this.multipartBuilder.addPart(part);
  }
  
  void setBody(RequestBody body) {
    this.body = body;
  }
  
  <T> void addTag(Class<T> cls, @Nullable T value) {
    this.requestBuilder.tag(cls, value);
  }
  
  Request.Builder get() {
    HttpUrl url;
    HttpUrl.Builder urlBuilder = this.urlBuilder;
    if (urlBuilder != null) {
      url = urlBuilder.build();
    } else {
      url = this.baseUrl.resolve(this.relativeUrl);
      if (url == null)
        throw new IllegalArgumentException("Malformed URL. Base: " + this.baseUrl + ", Relative: " + this.relativeUrl); 
    }

    RequestBody body = this.body;

    if (body == null)
      if (this.formBuilder != null) {
        FormBody formBody = this.formBuilder.build();
      } else if (this.multipartBuilder != null) {
        MultipartBody multipartBody = this.multipartBuilder.build();
      } else if (this.hasBody) {
        body = RequestBody.create(null, new byte[0]);
      }

    MediaType contentType = this.contentType;

    if (contentType != null)
      if (body != null) {
        body = new ContentTypeOverridingRequestBody(body, contentType);
      } else {
        this.headersBuilder.add("Content-Type", contentType.toString());
      }

    return this.requestBuilder.url(url).headers(this.headersBuilder.build()).method(this.method, body);
  }
  
  private static class ContentTypeOverridingRequestBody extends RequestBody {
    private final RequestBody delegate;
    
    private final MediaType contentType;
    
    ContentTypeOverridingRequestBody(RequestBody delegate, MediaType contentType) {
      this.delegate = delegate;
      this.contentType = contentType;
    }
    
    public MediaType contentType() {
      return this.contentType;
    }
    
    public long contentLength() throws IOException {
      return this.delegate.contentLength();
    }
    
    public void writeTo(BufferedSink sink) throws IOException {
      this.delegate.writeTo(sink);
    }
  }
}
