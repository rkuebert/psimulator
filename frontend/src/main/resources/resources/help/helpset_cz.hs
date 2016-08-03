<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE helpset
  PUBLIC "-//Sun Microsystems Inc.//DTD JavaHelp HelpSet Version 1.0//EN"
         "http://java.sun.com/products/javahelp/helpset_1_0.dtd">
<helpset version="1.0" xml:lang="cz-CZ">
   <!-- title -->
   <title>Nápověda</title>

   <!-- maps -->
   <maps>
     <homeID>item000</homeID>
     <mapref location="cz/help_map.jhm" />
   </maps>

   <!-- views -->
   <view>
      <name>TOC</name>
      <label>Obsah</label>
      <type>javax.help.TOCView</type>
      <data>cz/help_toc.xml</data>
      <image>book_icon</image>
   </view>

   <view xml:lang="cz-CZ">
     <name>Search</name>
     <label>Vyhledávání</label>
     <type>javax.help.SearchView</type>
     <data engine="com.sun.java.help.search.DefaultSearchEngine">JavaHelpSearch/cz</data>
     <image>search_icon</image>
  </view>

  <presentation default="true">
      <size width="800" height="600" />
      <image>help_icon</image>
      
      <toolbar>
        <helpaction image="previous_icon">javax.help.BackAction</helpaction>
        <helpaction image="next_icon">javax.help.ForwardAction</helpaction>
        <helpaction>javax.help.SeparatorAction</helpaction>
        <helpaction image="home_icon">javax.help.HomeAction</helpaction>
        <helpaction>javax.help.SeparatorAction</helpaction>
        <helpaction image="print_icon">javax.help.PrintAction</helpaction>
        <helpaction image="print_setup_icon">javax.help.PrintSetupAction</helpaction>
      </toolbar>
  </presentation>

</helpset>
