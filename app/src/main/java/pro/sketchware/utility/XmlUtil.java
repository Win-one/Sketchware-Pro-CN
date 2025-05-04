package pro.sketchware.utility;

import android.widget.Toast;

import mod.hey.studios.util.Helper;
import pro.sketchware.R;

public class XmlUtil {
    public static String replaceXml(String text) {
        return text.replace("<?xml version=\"1.0\" encoding=\"utf-8\"?>", "")
                .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\" ?>", "")
                .replace("\r", "")
                .replace("\n", "")
                .replace(" ", "")
                .replace("\t", "");
    }

    public static void saveXml(String path, String xml) {
        FileUtil.writeFile(path, xml);
        SketchwareUtil.toast(Helper.getResString(R.string.save_completed), Toast.LENGTH_SHORT);
    }

}
