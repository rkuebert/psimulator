package psimulator.dataLayer.Singletons.ImageFactory;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 *
 * @author Martin Švihlík <svihlma1 at fit.cvut.cz>
 */
public class ImageBuffer {
    /* Data structures for buffering */
    // String is path to image = the identificator
    private HashMap<String,HashMap<Integer, Image>> hwComponentBuffer;
    private HashMap<String,HashMap<Integer, Image>> hwMarkedComponentBuffer;
    //
    private HashMap<String,HashMap<Integer, Image>> textLabelsBuffer;
    
    public ImageBuffer(){
        // create EnumMap with all HW components
        hwComponentBuffer = new HashMap<String,HashMap<Integer, Image>>();
        hwMarkedComponentBuffer = new HashMap<String,HashMap<Integer, Image>>();
        //
        textLabelsBuffer = new HashMap<String,HashMap<Integer, Image>>();
    }
    
    /**
     * Clears alll Images in buffer
     */
    public void clearBuffer(){
        // each HW components HashMap is cleared
        for(Entry<String,HashMap<Integer, Image>> e : hwComponentBuffer.entrySet()){
            e.getValue().clear();
        }
        
        for(Entry<String,HashMap<Integer, Image>> e : hwMarkedComponentBuffer.entrySet()){
            e.getValue().clear();
        }
        
        for(Entry<String,HashMap<Integer, Image>> e : textLabelsBuffer.entrySet()){
            e.getValue().clear();
        }
    }
    
    /**
     * Clears Text image buffer
     */
    public void clearTextBuffers(){
        for(Entry<String,HashMap<Integer, Image>> e : textLabelsBuffer.entrySet()){
            e.getValue().clear();
        }
    }
    
    /**
     * Puts Image into buffer
     * @param text Text of string
     * @param size Font size
     * @param image 
     */
    public void putBufferedImageWithText(String text, Integer fontSize, Image image){
        // if map does not contains path
        if(!textLabelsBuffer .containsKey(text)){
            textLabelsBuffer .put(text, new HashMap<Integer, Image>());
        }
        
        textLabelsBuffer .get(text).put(fontSize, image);
    }
    
        
    /**
     * Gets specified Image
     * @param text Text of string
     * @param size Font size
     * @return Image if found, otherwise null
     */
    public BufferedImage getBufferedImageWithText(String text, Integer fontSize){
        // if map does not contains path
        if(!textLabelsBuffer.containsKey(text)){
            return null;
        }
        
        // if is specified BufferedImage in buffer
        if(textLabelsBuffer.get(text).containsKey(fontSize)){
            return (BufferedImage) textLabelsBuffer.get(text).get(fontSize);
        }else{
            // if isn't
            return null;
        }
    }
    
    /**
     * Puts Image into buffer
     * @param path
     * @param size
     * @param image 
     * @param marked 
     */
    public void putBufferedImage(String path, Integer size, Image image, boolean marked){
        HashMap<String,HashMap<Integer, Image>> map;

        if(marked){
            map = hwMarkedComponentBuffer;
        }else{
            map = hwComponentBuffer;
        }
        
        // if map does not contains path
        if(!map.containsKey(path)){
            map.put(path, new HashMap<Integer, Image>());
        }
        
        map.get(path).put(size, image);
    }
    
    /**
     * Gets specified Image
     * @param path
     * @param size
     * @return Image if found, otherwise null
     * @param marked 
     */
    public BufferedImage getBufferedImage(String path, Integer size, boolean marked){
        HashMap<String,HashMap<Integer, Image>> map;
        
        if(marked){
            map = hwMarkedComponentBuffer;
        }else{
            map = hwComponentBuffer;
        }
        
        // if map does not contains path
        if(!map.containsKey(path)){
            return null;
        }
        
        // if is specified BufferedImage in buffer
        if(map.get(path).containsKey(size)){
            return (BufferedImage) map.get(path).get(size);
        }else{
            // if isn't
            return null;
        }
    }
    
    
}
