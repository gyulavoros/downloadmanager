package hu.gyulavoros.downloadmanager.download;

public final class DownloadModel {

    private long downloadedBytes;
    private String id;
    private String httpUrl;
    private String outputFolder;

    public DownloadModel() {
        // empty constructor
    }

    public DownloadModel(final String id, final String httpUrl, final String outputFolder, final long downloadedBytes) {
        this.id = id;
        this.downloadedBytes = downloadedBytes;
        this.httpUrl = httpUrl;
        this.outputFolder = outputFolder;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getHttpUrl() {
        return httpUrl;
    }

    public void setHttpUrl(final String httpUrl) {
        this.httpUrl = httpUrl;
    }

    public String getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(final String outputFolder) {
        this.outputFolder = outputFolder;
    }

    public long getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(final long downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

}
