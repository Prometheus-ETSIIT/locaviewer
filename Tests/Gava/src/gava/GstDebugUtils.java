/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package gava;

import org.gstreamer.Bin;
import org.gstreamer.lowlevel.GstNative;

/**
 * Loads and saves pipelines in an XML file
 */
public class GstDebugUtils {

        public static int GST_DEBUG_GRAPH_SHOW_MEDIA_TYPE = (1 << 0);
        public static int GST_DEBUG_GRAPH_SHOW_CAPS_DETAILS = (1 << 1);
        public static int GST_DEBUG_GRAPH_SHOW_NON_DEFAULT_PARAMS = (1 << 2);
        public static int GST_DEBUG_GRAPH_SHOW_STATES = (1 << 3);
        public static int GST_DEBUG_GRAPH_SHOW_ALL = ((1 << 4) - 1);

        private static interface GstDebugAPI extends com.sun.jna.Library {
                void _gst_debug_bin_to_dot_file(Bin bin, int details, String
fileName);
        }

        private static final GstDebugAPI gst =
GstNative.load(GstDebugAPI.class);

        public static final void gstDebugBinToDotFile(Bin bin, int details,
                        String fileName) {
                gst._gst_debug_bin_to_dot_file(bin, details, fileName);
        }
}

