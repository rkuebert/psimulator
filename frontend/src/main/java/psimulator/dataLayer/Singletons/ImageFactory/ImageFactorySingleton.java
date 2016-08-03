package psimulator.dataLayer.Singletons.ImageFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import javax.swing.ImageIcon;
import psimulator.dataLayer.Enums.ToolbarIconSizeEnum;
import shared.Components.HwTypeEnum;
import shared.SimulatorEvents.SerializedComponents.PacketType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.MainTool;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.PacketImageType;
import psimulator.userInterface.SimulatorEditor.DrawPanel.Enums.SecondaryTool;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ImageFactorySingleton {

    BufferedImage bufferedImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g2 = (Graphics2D) bufferedImage.getGraphics();
    
    protected ImageBuffer imageBuffer;
    protected BufferedImageLoader bufferedImageLoader;
    // scales 3 je alpha
    private float[] scales = {1f, 1f, 1f, 1f};
    private float[] offsets = {50f, 50f, 50f, 1f};
    protected RescaleOp rescaleOp = new RescaleOp(scales, offsets, null);

    private ImageFactorySingleton() {
        this.imageBuffer = new ImageBuffer();
        this.bufferedImageLoader = new BufferedImageLoader();

        // preload images from file
        preLoadAllImagesFromFiles();

        // get some font metrics to avoid long time when first component is placed into draw panel
        Font font = new Font("SanSerif", Font.PLAIN, 12);
        getFontMetrics(font);
    }

    public static ImageFactorySingleton getInstance() {
        return ImageFactorySingletonHolder.INSTANCE;
    }

    private static class ImageFactorySingletonHolder {

        private static final ImageFactorySingleton INSTANCE = new ImageFactorySingleton();
    }

    public final FontMetrics getFontMetrics(Font font) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        return fm;
    }

    /**
     * Prealoads all images from files.
     */
    private void preLoadAllImagesFromFiles() {
        HwTypeEnum hwTypes[] = HwTypeEnum.values();
        for (HwTypeEnum hwType : hwTypes) {
            String path = getImagePath(hwType);
            try {
                bufferedImageLoader.getImage(path);
            } catch (IOException ex) {
                // should never happen
            }
        }

        //PacketType packetType, PacketImageType packageImageType

        PacketType packetTypes[] = PacketType.values();
        PacketImageType packetImageTypes[] = PacketImageType.values();

        for (PacketType packetType : packetTypes) {
            for (PacketImageType packetImageType : packetImageTypes) {
                String path = getImagePath(packetType, packetImageType);
                try {
                    bufferedImageLoader.getImage(path);
                } catch (IOException ex) {
                    // should never happen
                }
            }
        }

    }

    /**
     * Gets image icon from path.
     *
     * @param path
     * @return
     */
    public ImageIcon getImageIcon(String path) {
        ImageIcon image = bufferedImageLoader.getImageIcon(path);
        return image;
    }

    /**
     * Gets image with desired text with Font size fontSize and desired width
     * and height. It is buffered.
     *
     * @param text
     * @param fontSize
     * @param textWidth
     * @param textHeigh
     * @param ascent
     * @return
     */
    public BufferedImage getImageWithText(String text, Font font, int textWidth, int textHeigh, int ascent) {
        BufferedImage image;

        image = imageBuffer.getBufferedImageWithText(text, font.getSize());

        if (image == null) {
            //System.out.println("MISS");
            // load image from file
            image = createImageWithText(text, font, textWidth, textHeigh, ascent);
            // put image into buffer
            imageBuffer.putBufferedImageWithText(text, font.getSize(), image);
        } else {
            //System.out.println("HIT");
        }

        return image;
    }

    /**
     * Creates BufferImage with text painted in black with white edge of sizes
     * in parameters.
     *
     * @param text
     * @param font
     * @param textWidth
     * @param textHeight
     * @param ascent
     * @return
     */
    protected BufferedImage createImageWithText(String text, Font font, int textWidth, int textHeight, int ascent) {
        textWidth = textWidth + 2;
        textHeight = textHeight + 2;

        BufferedImage bi = new BufferedImage(textWidth, textHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = bi.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        int x = 1;
        //int y = textHeight;
        int y = ascent;

        g2.setFont(font);

        // paint white border
        g2.setColor(Color.WHITE);
        g2.drawString(text, x + 1, y + 1);
        g2.drawString(text, x + 1, y - 1);
        g2.drawString(text, x - 1, y + 1);
        g2.drawString(text, x - 1, y - 1);

        // paint the black text
        g2.setColor(Color.BLACK);
        g2.drawString(text, x, y);

        g2.dispose();

        return bi;
    }

    /**
     * Returns BufferedImage for hwComponentType at path of size. If marked, the
     * result image is brighter by 50%.
     *
     * @param hwComponentType
     * @param width
     * @param marked
     * @return
     */
    public BufferedImage getImage(HwTypeEnum hwComponentType, Integer width, boolean marked) {
        BufferedImage image;
        String path = getImagePath(hwComponentType);

        image = imageBuffer.getBufferedImage(path, width, marked);

        if (image == null) {
            // load image from file
            image = createImage(hwComponentType, path, width, marked);
            // put image into buffer
            imageBuffer.putBufferedImage(path, width, image, marked);
        }

        return image;
    }

    /**
     * Returns BufferedImage for packetType and packetImageType with width.
     *
     * @param packetType
     * @param packageImageType
     * @param width
     * @return
     */
    public BufferedImage getPacketImage(PacketType packetType, PacketImageType packageImageType, int width) {
        BufferedImage image;

        String path = getImagePath(packetType, packageImageType);

        image = imageBuffer.getBufferedImage(path, width, false);

        if (image == null) {
            // load image from file
            image = getScaledBufferedImage(path, width);
            // put image into buffer
            imageBuffer.putBufferedImage(path, width, image, false);
        }

        return image;
    }

    /**
     * Returns BufferedImage for hwComponentType at path of size. If marked, the
     * result image is brighter by 50%.
     *
     * @param hwComponentType
     * @param path
     * @param width
     * @param marked
     * @return
     */
    public BufferedImage getImage(HwTypeEnum hwComponentType, String path, Integer width, boolean marked) {
        BufferedImage image;

        image = imageBuffer.getBufferedImage(path, width, marked);

        if (image == null) {
            // load image from file
            image = createImage(hwComponentType, path, width, marked);
            // put image into buffer
            imageBuffer.putBufferedImage(path, width, image, marked);
        }

        return image;
    }

    /**
     * Creates BufferedImage for hwComponentType at path of size. If marked, the
     * result image is brighter by 50%.
     *
     * @param hwComponentType
     * @param path
     * @param width
     * @param marked
     * @return
     */
    protected BufferedImage createImage(HwTypeEnum hwComponentType, String path, Integer width, boolean marked) {

        BufferedImage bi = null;

        Image tmp = null;

        try {
            // loads image from path and scales it to desired size
            tmp = getScaledImage(path, width);
        } catch (IOException ex) {
            try {
                // load default image
                tmp = getScaledImage(getImagePath(hwComponentType), width);
            } catch (IOException ex1) {
                // should never happen, all hwComponentType default icons are in .jar as a resource
            }
        }

        // create new buffered image to paint on
        bi = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // create graphics and set hints
        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // draw image
        graphics2D.drawImage(tmp, 0, 0, null);

        // if marked, draw transparent white rectangle over
        if (marked) {
            rescaleOp.filter(bi, bi);
        }

        graphics2D.dispose();

        return bi;
    }

    /**
     * Returns default image for MainTool tool.
     *
     * @param tool
     * @return ImageIcon with ICON_SIZE_MENU_BAR size
     */
    public ImageIcon getImageIconForToolbar(MainTool tool) {
        String path;

        switch (tool) {
            case HAND:
                path = TOOL_HAND_PATH;
                break;
            case ADD_END_DEVICE:
                path = TOOL_END_DEVICE_PC_PATH;
                break;
            case ADD_ROUTER:
                path = TOOL_ROUTER_PATH;
                break;
            case ADD_SWITCH:
                path = TOOL_SWITCH_PATH;
                break;
            case ADD_CABLE:
                path = TOOL_CABLE_ETHERNET_PATH;
                break;
            case ADD_REAL_PC:
            default:
                path = TOOL_REAL_PC_PATH;
                break;
        }

        ImageIcon tmp = null;

        // load image
        try {
            //tmp = new ImageIcon(getScaledImage(path, ICON_SIZE_MENU_BAR));
            tmp = createSquareImage(getScaledImage(path, ICON_SIZE_MENU_BAR));
        } catch (IOException ex) {
            // should never happen, all hwComponentType default icons are in .jar as a resource
        }

        // return scaled image
        return tmp;
    }

    /**
     * Returns image for MainTool tool at path. If image at path could not be
     * loaded, the default image for tool is returned.
     *
     * @param tool
     * @param path
     * @return ImageIcon with ICON_SIZE_MENU_BAR size.
     */
    public ImageIcon getImageIconForToolbar(MainTool tool, String path, ToolbarIconSizeEnum size, boolean popup) {
        ImageIcon icon = null;

        int iconSize = getIconSizeForEditorToolBar(size, popup);



        if (path == null) {
            icon = getImageIconForToolbar(tool);
        } else {
            try {
                icon = createSquareImage(getScaledImage(path, iconSize));
            } catch (Exception e) {
                System.out.println("chyba pri nacitani obrazku");
                icon = getImageIconForToolbar(tool);
            }
        }

        return icon;
    }

    private int getIconSizeForEditorToolBar(ToolbarIconSizeEnum size, boolean popup) {
        int iconSize;

        switch (size) {
            case TINY:
                iconSize = 22;
                break;
            case SMALL:
                iconSize = 32;
                break;
            case MEDIUM:
                iconSize = 48;
                break;
            case LARGE:
            default:
                iconSize = 60;
                break;
        }

        if (popup) {
            iconSize = iconSize - (int) (iconSize * 0.3);
        }

        return iconSize;
    }

    public ImageIcon getImageIconForToolbar(SecondaryTool tool, ToolbarIconSizeEnum size) {
        String path;

        int iconSize = getIconSizeForEditorToolBar(size, false);

        switch (tool) {
            case ALIGN_TO_GRID:
                path = TOOL_ALIGN_TO_GRID_PATH;
                break;
            case FIT_TO_SIZE:
            default:
                path = TOOL_FIT_TO_SIZE_PATH;
                break;
        }

        ImageIcon tmp = null;

        // load image
        try {
            //tmp = new ImageIcon(getScaledImage(path, ICON_SIZE_MENU_BAR));
            tmp = createSquareImage(getScaledImage(path, iconSize));
        } catch (IOException ex) {
            // should never happen, all hwComponentType default icons are in .jar as a resource
        }

        // return scaled image
        return tmp;
    }
    
    /**
     * Gets image for component properties dialog
     * @param hwComponentType
     * @return 
     */
    public ImageIcon getDialogIconForComponentProperties(HwTypeEnum hwComponentType){
        String path = getImagePath(hwComponentType);
        
        ImageIcon tmp = null;
        
        try {
            tmp = createSquareImage(getScaledImage(path, 32));
        } catch (IOException ex) {
            // should never happen, all hwComponentType default icons are in .jar as a resource
        }
        return tmp;
    }

    /**
     * Creates square image of given image
     *
     * @param image
     * @return
     */
    private ImageIcon createSquareImage(Image image) {
        int size = image.getWidth(null);

        int offsetHeight = (image.getWidth(null) - image.getHeight(null)) / 2;

        BufferedImage bi = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

        // create graphics and set hints
        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // draw image
        graphics2D.drawImage(image, 0, offsetHeight, null);

        graphics2D.dispose();

        return new ImageIcon(bi);
    }

    /**
     * Creates buffered image from path of width.
     *
     * @param path
     * @param width
     * @return
     * @throws IOException
     */
    private BufferedImage getScaledBufferedImage(String path, int width) {
        BufferedImage bi;

        Image tmp = null;

        try {
            // loads image from path and scales it to desired size
            tmp = getScaledImage(path, width);
        } catch (IOException ex) {
            // should never happen, all hwComponentType default icons are in .jar as a resource
        }

        // create new buffered image to paint on
        bi = new BufferedImage(tmp.getWidth(null), tmp.getHeight(null), BufferedImage.TYPE_INT_ARGB);

        // create graphics and set hints
        Graphics2D graphics2D = bi.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        // draw image
        graphics2D.drawImage(tmp, 0, 0, null);

        graphics2D.dispose();

        return bi;
    }

    /**
     * Creates scaled image of image at path. Size will be size x size.
     *
     * @param path
     * @param width
     * @return scaled image
     * @throws IOException
     */
    private Image getScaledImage(String path, int width) throws IOException {
        Image tmp = bufferedImageLoader.getImage(path);
        int height = (int) ((double) (tmp.getHeight(null) / (double) tmp.getWidth(null)) * width);
        tmp = tmp.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return tmp;
    }

    /**
     * Clears all image buffers.
     */
    public void clearBuffer() {
        imageBuffer.clearBuffer();
    }

    /**
     * Clears buffers with text labels. Call when project is closed.
     */
    public void clearTextBuffers() {
        imageBuffer.clearTextBuffers();
    }

    /**
     * Gets pcket image path of specified type and color.
     * @param packetType
     * @param packageImageType
     * @return 
     */
    private String getImagePath(PacketType packetType, PacketImageType packageImageType) {
        String middle;
        String suffix;

        switch (packetType) {
            case ARP:
                suffix = "yellow.png";
                break;
            case ICMP:
                suffix = "gray.png";
                break;
            case TCP:
                suffix = "green.png";
                break;
            case UDP:
                suffix = "blue.png";
                break;
            case IP:
            case ETHERNET:
                suffix = "black.png";
                break;
            case GENERIC:
            default:
                suffix = "pink.png";
                break;
        }

        switch (packageImageType) {
            case CAR:
                middle = "delivery_truck";
                break;
            case CLASSIC:
                middle = "package";
                break;
            case ENVELOPE:
            default:
                middle = "envelope";
                break;
        }

        return PACKAGE_PREFIX_PATH + middle + "_" + suffix;
        //return PACKAGE_PREFIX_PATH + middle + ".png";
    }

    /**
     * Gets image path for gw type
     * @param type
     * @return 
     */
    private String getImagePath(HwTypeEnum type) {
        switch (type) {
            case CISCO_ROUTER:
                return TOOL_ROUTER_CISCO_PATH;
            case LINUX_ROUTER:
                return TOOL_ROUTER_LINUX_PATH;
            case CISCO_SWITCH:
                return TOOL_SWITCH_CISCO_PATH;
            case LINUX_SWITCH:
                //return TOOL_SWITCH_LINUX_PATH;
                return TOOL_SWITCH_PATH;
            case END_DEVICE_PC:
                return TOOL_END_DEVICE_PC_PATH;
            case END_DEVICE_NOTEBOOK:
                return TOOL_END_DEVICE_NOTEBOOK_PATH;
            case END_DEVICE_WORKSTATION:
                return TOOL_END_DEVICE_WORKSTATION_PATH;
            case CABLE_ETHERNET:
                return TOOL_CABLE_ETHERNET_PATH;
            case CABLE_OPTIC:
                return TOOL_CABLE_OPTICS_PATH;
            case REAL_PC:
            default:
                return TOOL_REAL_PC_PATH;
        }
    }
    
    
    //
    public static final int ICON_SIZE_MENU_BAR = 48;
    public static final int ICON_SIZE_MENU_BAR_POPUP = 30;
    //
    public static final String ICON_HOME_48_PATH = "/resources/toolbarIcons/48/home.png";
    public static final String ICON_TERMINAL_32_PATH = "/resources/toolbarIcons/32/terminal.png";
    //
    public static final String TOOL_DRAG_MOVE_PATH = "/resources/toolbarIcons/editor_toolbar/move.png";
    public static final String TOOL_HAND_PATH = "/resources/toolbarIcons/editor_toolbar/cursor_hand_mod_2.png";
    public static final String TOOL_CABLE_ETHERNET_PATH = "/resources/toolbarIcons/editor_toolbar/network-wired.png";
    public static final String TOOL_CABLE_OPTICS_PATH = "/resources/toolbarIcons/editor_toolbar/network-wired_gray.png";
    public static final String TOOL_REAL_PC_PATH = "/resources/toolbarIcons/editor_toolbar/local_network.png";
    public static final String TOOL_ALIGN_TO_GRID_PATH = "/resources/toolbarIcons/editor_toolbar/grid.png";
    public static final String TOOL_FIT_TO_SIZE_PATH = "/resources/toolbarIcons/editor_toolbar/fit_to_size.png";
    //
    public static final String TOOL_END_DEVICE_WORKSTATION_PATH = "/resources/toolbarIcons/editor_toolbar/desktop.png";
    public static final String TOOL_END_DEVICE_PC_PATH = "/resources/toolbarIcons/editor_toolbar/pc.png";
    public static final String TOOL_END_DEVICE_NOTEBOOK_PATH = "/resources/toolbarIcons/editor_toolbar/notebook.png";
    public static final String TOOL_ROUTER_PATH = "/resources/toolbarIcons/editor_toolbar/router.png";
    public static final String TOOL_ROUTER_LINUX_PATH = "/resources/toolbarIcons/editor_toolbar/router_linux.png";
    public static final String TOOL_ROUTER_CISCO_PATH = "/resources/toolbarIcons/editor_toolbar/router_cisco.png";
    public static final String TOOL_SWITCH_PATH = "/resources/toolbarIcons/editor_toolbar/switch.png";
    public static final String TOOL_SWITCH_LINUX_PATH = "/resources/toolbarIcons/editor_toolbar/switch_linux.png";
    public static final String TOOL_SWITCH_CISCO_PATH = "/resources/toolbarIcons/editor_toolbar/switch_cisco.png";
    //
    public static final String ICON_FILENEW_128_PATH = "/resources/toolbarIcons/128/filenew.png";
    public static final String ICON_OPENFILE_GREEN_128_PATH = "/resources/toolbarIcons/128/folder_green_open.png";
    public static final String ICON_ETH_INTERFACE_16_PATH = "/resources/toolbarIcons/16/eth_interface_image.png";
    //
    public static final String ICON_CONFIGURE_16_PATH = "/resources/toolbarIcons/16/configure.png";
    public static final String ICON_GRID_16_PATH = "/resources/toolbarIcons/16/grid.png";
    public static final String ICON_CANCEL_16_PATH = "/resources/toolbarIcons/16/button_cancel.png";
    public static final String ICON_SELECT_ALL_16_PATH = "/resources/toolbarIcons/16/select_all.png";
    public static final String ICON_FIT_TO_SIZE_16_PATH = "/resources/toolbarIcons/16/fit_to_size.png";
    public static final String ICON_STOCK_ALIGNMENT_16_PATH = "/resources/toolbarIcons/16/stock_alignment.png";
    public static final String ICON_FILENEW_16_PATH = "/resources/toolbarIcons/16/filenew.png";
    public static final String ICON_FILECLOSE_16_PATH = "/resources/toolbarIcons/16/fileclose.png";
    public static final String ICON_OPEN_GREEN_16_PATH = "/resources/toolbarIcons/16/folder_green_open.png";
    public static final String ICON_FILESAVE_16_PATH = "/resources/toolbarIcons/16/filesave.png";
    public static final String ICON_FILESAVEAS_16_PATH = "/resources/toolbarIcons/16/filesaveas.png";
    public static final String ICON_TELNET_16_PATH = "/resources/toolbarIcons/16/terminal.png";
    public static final String ICON_INFO_16_PATH = "/resources/toolbarIcons/16/messagebox_info.png";
    //
    public static final String PACKAGE_PREFIX_PATH = "/resources/toolbarIcons/simulator/packages/";
    //
}
