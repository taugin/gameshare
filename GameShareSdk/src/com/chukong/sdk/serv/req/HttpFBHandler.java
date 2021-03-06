package com.chukong.sdk.serv.req;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.RequestLine;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import se.unlogic.standardutils.crypto.Base64;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import com.chukong.sdk.Constants.Config;
import com.chukong.sdk.GlobalInit;
import com.chukong.sdk.common.Log;
import com.chukong.sdk.serv.req.objs.FileRow;
import com.chukong.sdk.serv.req.objs.TwoColumn;
import com.chukong.sdk.serv.support.Progress;
import com.chukong.sdk.serv.view.ViewFactory;
import com.chukong.sdk.util.CommonUtil;

/**
 * @brief 目录浏览页面请求处理
 * @author join
 */
public class HttpFBHandler implements HttpRequestHandler {

    private CommonUtil mCommonUtil = CommonUtil.getSingleton();
    private ViewFactory mViewFactory = ViewFactory.getSingleton();

    private String webRoot;

    public HttpFBHandler(final String webRoot) {
        this.webRoot = webRoot;
    }

    @Override
    public void handle(HttpRequest request, HttpResponse response,
            HttpContext context) throws HttpException, IOException {
        String target = URLDecoder.decode(request.getRequestLine().getUri(),
                Config.ENCODING);
        Header requestHost = request.getFirstHeader("Host");
        InetAddress ipAddress = (InetAddress) context
                .getAttribute("remote_ip_address");
        Log.d(Log.TAG, "requestHost = " + requestHost);
        Log.d(Log.TAG, "clientIpAddress = " + ipAddress.getHostAddress()
                + " , target = " + target);
        String requestMethod = null;
        RequestLine requestLine = request.getRequestLine();
        if (requestLine != null) {
            requestMethod = requestLine.getMethod();
        }
        if (target.startsWith("/download")) {
            target = target.substring("/download".length());
        }
        File file;
        if (target.equals("/view.html")) {
            file = new File(this.webRoot);
        } else if (!target.startsWith(Config.SERV_ROOT_DIR)
                && !target.startsWith(this.webRoot)
                && !target.startsWith("/data/app")
                && !target.startsWith("/system/app")) {
            String ip = mCommonUtil.getLocalIpAddress();
            String hostAddress = requestHost != null ? requestHost.getValue()
                    : null;
            int port = Config.PORT;
            String localHost = ip + ":" + port;
            if (hostAddress != null && hostAddress.contains("127.0.0.1")) {
                ip = "http://127.0.0.1";
            }
            Log.d(Log.TAG, "localHost = " + (localHost) + " , requestMethod = "
                    + requestMethod);
            if ((localHost != null && localHost.equals(requestHost))
                    || (requestMethod != null && requestMethod
                            .equalsIgnoreCase("POST"))) {
                response.setStatusCode(HttpStatus.SC_FORBIDDEN);
                response.setEntity(resp403(request));
            } else {
                response.setStatusCode(301);
                if (!TextUtils.isEmpty(ip)) {
                    ip = ip + ":" + port;
                } else {
                    ip = "http://10.0.0.115:" + port;
                }
                if (!ip.startsWith("http")) {
                    ip = "http://" + ip;
                }
                ip += "/view.html";
                Log.d(Log.TAG, "Redirect ipaddress : " + ip);
                response.addHeader("Location", ip);
            }
            return;
        } else {
            file = new File(target);
        }
        HttpEntity entity;
        String contentType = "text/html;charset=" + Config.ENCODING;
        if (!file.exists()) { // 不存在
            response.setStatusCode(HttpStatus.SC_NOT_FOUND);
            entity = resp404(request);
        } else if (file.canRead()) { // 可读
            response.setStatusCode(HttpStatus.SC_OK);
            if (file.isDirectory()) {
                entity = respView(request, file);
            } else {
                entity = respFile(request, file);
                contentType = entity.getContentType().getValue();
            }
        } else { // 不可读
            response.setStatusCode(HttpStatus.SC_FORBIDDEN);
            entity = resp403(request);
        }

        response.setHeader("Content-Type", contentType);
        response.setEntity(entity);
        // Log.d(Log.TAG, "contentType = " + contentType);
        // printRequest(response);
        Progress.clear();
    }

    private HttpEntity respFile(HttpRequest request, File file)
            throws IOException {
        return mViewFactory.renderFile(request, file);
    }

    private HttpEntity resp403(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "403.html");
    }

    private HttpEntity resp404(HttpRequest request) throws IOException {
        return mViewFactory.renderTemp(request, "404.html");
    }

    private HttpEntity respView(HttpRequest request, File dir)
            throws IOException {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("dirpath", dir.getPath()); // 目录路径
        data.put("hasParent", !isSamePath(dir.getPath(), this.webRoot)); // 是否有上级目录
        List<FileRow> fileRows = buildFileRows(dir);
        data.put("fileRows", fileRows); // 文件行信息集合
        data.put("rowsCount", fileRows.size());
        return mViewFactory.renderTemp(request, "view.html", data);
    }

    private boolean isSamePath(String a, String b) {
        String left = a.substring(b.length(), a.length()); // a以b开头
        if (left.length() >= 2) {
            return false;
        }
        if (left.length() == 1 && !left.equals("/")) {
            return false;
        }
        return true;
    }

    private List<TwoColumn> buildTwoColumn(File dir) {
        List<FileRow> fileRows = buildFileRows(dir);
        if (fileRows == null) {
            return null;
        }
        int index = 0;
        int len = fileRows.size();
        List<TwoColumn> twoColumns = new ArrayList<TwoColumn>();
        TwoColumn twoColumn = null;
        for (index = 0; index < len; index += 2) {
            twoColumn = new TwoColumn();
            if (index < len) {
                twoColumn.fileRow1 = fileRows.get(index);
            }
            if (index + 1 < len) {
                twoColumn.fileRow2 = fileRows.get(index + 1);
            }
            twoColumns.add(twoColumn);
        }
        return twoColumns;
    }

    private File[] getFileFromDataApp() {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        List<ApplicationInfo> apps = pm.getInstalledApplications(0);
        if (apps == null) {
            return null;
        }
        List<File> appFiles = new ArrayList<File>();
        File file = null;
        for (ApplicationInfo info : apps) {
            if (filterPackage(info)) {
                continue;
            }
            file = new File(info.publicSourceDir);
            appFiles.add(file);
        }
        return appFiles.toArray(new File[appFiles.size()]);
    }

    private boolean filterPackage(ApplicationInfo filterInfo) {
        if (filterInfo == null) {
            return true;
        }
        /*
         * if ((filterInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) { return
         * true; }
         */
        if (filterInfo.publicSourceDir.startsWith("/system")) {
            return true;
        }
        String packageName = filterInfo.packageName;
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        String thisPackage = null;
        if (pm != null) {
            ApplicationInfo info = null;
            try {
                info = pm.getApplicationInfo(context.getPackageName(), 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            if (info != null) {
                thisPackage = info.packageName;
            }
        }
        if ("com.gtja.dzh".equals(packageName)
                || "com.hexin.plat.android".equals(packageName)
                || "com.eastmoney.android.berlin".equals(packageName)) {
            return true;
        }
        if (thisPackage != null && thisPackage.equals(packageName)) {
            return true;
        }
        return false;
    }

    private File getThisAppFile() {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        ApplicationInfo info = null;
        try {
            info = pm.getApplicationInfo(context.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info != null && info.publicSourceDir != null) {
            return new File(info.publicSourceDir);
        }
        return null;
    }

    private List<FileRow> buildFileRows(File dir) {
        File[] files = dir.listFiles(mFilter); // 目录列表
        if (files != null) {
            sort(files); // 排序
            ArrayList<FileRow> fileRows = new ArrayList<FileRow>();
            boolean localShare = GlobalInit.getInstance().getLocalShare();
            if (!localShare) {
                for (File file : files) {
                    fileRows.add(buildFileRow(file, false));
                }
                if (Config.SHOW_INSTALLED_APP) {
                    File appFiles[] = getFileFromDataApp();
                    sort(appFiles);
                    for (File file : appFiles) {
                        if (file != null && file.length() >= 1024 * 1024) {
                            fileRows.add(buildFileRow(file, true));
                        }
                    }
                }
            }
            File thisFile = getThisAppFile();
            if (thisFile != null) {
                Log.d(Log.TAG, "thisFile = " + thisFile);
                fileRows.add(0, buildFileRow(thisFile, true));
            }
            sortList(fileRows);
            return fileRows;
        }
        return null;
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd ahh:mm");

    private FileRow buildFileRow(File f, boolean installed) {
        boolean isDir = f.isDirectory();
        String clazz, name, link, size, icon = null, desc = null;
        long numSize = 0;
        if (isDir) {
            clazz = "icon dir";
            name = f.getName() + "/";
            link = f.getPath() + "/";
            size = "";
            numSize = 0;
        } else {
            clazz = "icon file";
            name = f.getName();
            link = f.getPath();
            if (name != null && name.endsWith(".apk")) {
                String label = getApkName(link);
                if (label != null) {
                    name = label;
                }
                icon = getApkIcon(link);
            }
            size = mCommonUtil.readableFileSize(f.length());
            numSize = f.length();
        }
        // desc = installed ? "Installed" : "UnInstalled";
        desc = link;
        FileRow row = new FileRow(clazz, name, link, size, icon, desc, numSize);
        row.time = sdf.format(new Date(f.lastModified()));
        if (f.canRead()) {
            row.can_browse = true;
            if (Config.ALLOW_DOWNLOAD && f.isFile()) {
                row.can_download = true;
            }
            if (f.canWrite()) {
                if (Config.ALLOW_DELETE) {
                    row.can_delete = true;
                }
                if (Config.ALLOW_UPLOAD && isDir) {
                    row.can_upload = true;
                }
            }
        }
        return row;
    }

    private String getApkName(String apkFile) {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                CharSequence label = appInfo.loadLabel(pm);
                if (label != null) {
                    return label.toString();
                }
            }
        }
        return null;
    }

    private String getApkIcon(String apkFile) {
        Context context = GlobalInit.getInstance().getBaseContext();
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }
        PackageInfo info = pm.getPackageArchiveInfo(apkFile,
                PackageManager.GET_ACTIVITIES);
        ApplicationInfo appInfo = null;
        if (info != null) {
            appInfo = info.applicationInfo;
            if (appInfo != null) {
                appInfo.publicSourceDir = apkFile;
                try {
                    Drawable drawable = appInfo.loadIcon(pm);
                    Bitmap bmp = null;
                    if (drawable instanceof BitmapDrawable) {
                        bmp = ((BitmapDrawable) drawable).getBitmap();
                    } else {
                        // TODO : 缺少默认图标
                        // bmp =
                        // BitmapFactory.decodeResource(context.getResources(),
                        // R.drawable.icon_default);
                    }
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    byte iconbyte[] = baos.toByteArray();
                    return Base64.encodeBytes(iconbyte);
                } catch (Exception e) {
                    Log.d(Log.TAG, "e = " + e);
                }
            }
        }
        return null;
    }

    /** 排序：文件夹、文件，再各安字符顺序 */
    private void sort(File[] files) {
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File f1, File f2) {
                if (f1.isDirectory() && !f2.isDirectory()) {
                    return -1;
                } else if (!f1.isDirectory() && f2.isDirectory()) {
                    return 1;
                } else {
                    return f1.toString().compareToIgnoreCase(f2.toString());
                }
            }
        });
    }

    // According to file size
    private void sortList(ArrayList<FileRow> fileRows) {
        Collections.sort(fileRows, new Comparator<FileRow>() {
            @Override
            public int compare(FileRow lhs, FileRow rhs) {
                return lhs.numSize < rhs.numSize ? 1 : -1;
            }
        });
    }

    private FileFilter mFilter = new FileFilter() {

        @Override
        public boolean accept(File pathname) {
            if (pathname.getName().endsWith(".apk")) {
                return true;
            }
            return false;
        }
    };

    private void printRequest(HttpRequest request) {
        Header headers[] = request.getAllHeaders();
        for (Header h : headers) {
            Log.d(Log.TAG, h.getName() + " : " + h.getValue());
        }
    }

    private void printResponse(HttpResponse res) {
        Header headers[] = res.getAllHeaders();
        for (Header h : headers) {
            Log.d(Log.TAG, h.getName() + " : " + h.getValue());
        }
    }
}
