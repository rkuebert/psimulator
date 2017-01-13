/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package shared.build;

/**
 *
 * @author roland
 */
public class BuildVersion {

    public static String getBuildVersion(){
        return BuildVersion.class.getPackage().getImplementationVersion();
    }

}
