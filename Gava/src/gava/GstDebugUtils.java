/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014 Prometheus
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
