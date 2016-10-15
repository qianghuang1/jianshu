public class MultipartRequest extends Request<String> {

    /* Used for debugging */
    private static final String TAG = MultipartRequest.class.getSimpleName();

    /* MediaTypes */
    public static final MediaType MEDIA_TYPE_JPEG = MediaType.parse("image/jpeg");
    public static final MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
    public static final MediaType MEDIA_TYPE_TEXT_PLAIN = MediaType.parse("text/plain");

    private MultipartBody.Builder mBuilder = new MultipartBody.Builder();
    private final Response.Listener<String> mListener;
    private RequestBody mRequestBody;
    public String key;

    public MultipartRequest(String url,
                                  Response.ErrorListener errorListener,
                                  Response.Listener<String> listener) {
        super(Method.POST, url, errorListener);
        mListener = listener;
        mBuilder.setType(MultipartBody.FORM);
    }

    /**
     * Adds a collection of string values to the request.
     * @param mParams {@link HashMap} collection of values to be added to the request.
     * */
    public void addStringParams(HashMap<String, String> mParams){
        for (Map.Entry<String, String> entry : mParams.entrySet()) {
            mBuilder.addPart(
                    Headers.of("Content-Disposition", "form-data; name=\"" + entry.getKey() + "\""),
                    RequestBody.create(MEDIA_TYPE_TEXT_PLAIN, entry.getValue()));
        }
    }

    /**
     * Adds a single value to the request.
     * @param key String - the field name.
     * @param value String - the field's value.
     * */
    public void addStringParam(String key, String value) {
        mBuilder.addPart(
                Headers.of("Content-Disposition", "form-data; name=\"" + key + "\""),
                RequestBody.create(MEDIA_TYPE_TEXT_PLAIN, value));
    }

    /**
     * Adds a binary attachment to the request.
     * @param content_type {@link MediaType} - the type of the attachment.
     * @param key String - the attachment field name.
     * @param value {@link File} - the file to be attached.
     * */
    public void addAttachment(MediaType content_type, String key, File value){
        mBuilder.addFormDataPart("file", key, RequestBody.create(content_type, value));
    }

    /**
     * Builds the request.
     * Must be called before adding the request to the Volley request queue.
     * */
    public void buildRequest(){
        mRequestBody = mBuilder.build();
    }

    @Override
    public String getBodyContentType() {
        return mRequestBody.contentType().toString();
    }

    @Override
    public byte[] getBody() throws AuthFailureError {
        Buffer buffer = new Buffer();
        try
        {
            buildRequest();
            mRequestBody.writeTo(buffer);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            VolleyLog.e("IOException writing to ByteArrayOutputStream");
        }
        return buffer.readByteArray();
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        String parsed;
        try {
            parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            parsed = new String(response.data);
        }
        return Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response));
    }

    @Override
    public Request<?> setRetryPolicy(RetryPolicy retryPolicy) {
        return super.setRetryPolicy(retryPolicy);
    }

    @Override
    protected void deliverResponse(String response) {
        if (mListener != null) {
            mListener.onResponse(response);
        }
    }
}
