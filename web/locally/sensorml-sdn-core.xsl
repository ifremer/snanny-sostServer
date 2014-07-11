<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:sch="http://www.ascc.net/xml/schematron"
                xmlns:iso="http://purl.oclc.org/dsdl/schematron"
                xmlns:gmd="http://www.isotc211.org/2005/gmd"
                xmlns:gco="http://www.isotc211.org/2005/gco"
                xmlns:sml="http://www.opengis.net/sensorml/2.0"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:gml="http://www.opengis.net/gml/3.2"
                xmlns:swe="http://www.opengis.net/swe/2.0"
                xmlns:skos="http://www.w3.org/2004/02/skos/core#"
                gmd:dummy-for-xmlns=""
                gco:dummy-for-xmlns=""
                sml:dummy-for-xmlns=""
                xlink:dummy-for-xmlns=""
                gml:dummy-for-xmlns=""
                swe:dummy-for-xmlns=""
                skos:dummy-for-xmlns=""
                version="1.0"><!--Implementers: please note that overriding process-prolog or process-root is 
    the preferred method for meta-stylesheets to use where possible. -->
   <xsl:param name="archiveDirParameter"/>
   <xsl:param name="archiveNameParameter"/>
   <xsl:param name="fileNameParameter"/>
   <xsl:param name="fileDirParameter"/>

   <!--PHASES-->


   <!--PROLOG-->
   <xsl:output xmlns:xs="http://www.w3.org/2001/XMLSchema"
               xmlns:schold="http://www.ascc.net/xml/schematron"
               xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
               method="xml"
               omit-xml-declaration="no"
               standalone="yes"
               indent="yes"/>

   <!--KEYS-->


   <!--DEFAULT RULES-->


   <!--MODE: SCHEMATRON-SELECT-FULL-PATH-->
   <!--This mode can be used to generate an ugly though full XPath for locators-->
   <xsl:template match="*" mode="schematron-select-full-path">
      <xsl:apply-templates select="." mode="schematron-get-full-path"/>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-->
   <!--This mode can be used to generate an ugly though full XPath for locators-->
   <xsl:template match="*" mode="schematron-get-full-path">
      <xsl:apply-templates select="parent::*" mode="schematron-get-full-path"/>
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">
            <xsl:value-of select="name()"/>
            <xsl:variable name="p_1"
                          select="1+    count(preceding-sibling::*[name()=name(current())])"/>
            <xsl:if test="$p_1&gt;1 or following-sibling::*[name()=name(current())]">[<xsl:value-of select="$p_1"/>]</xsl:if>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>*[local-name()='</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>' and namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
            <xsl:variable name="p_2"
                          select="1+   count(preceding-sibling::*[local-name()=local-name(current())])"/>
            <xsl:if test="$p_2&gt;1 or following-sibling::*[local-name()=local-name(current())]">[<xsl:value-of select="$p_2"/>]</xsl:if>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   <xsl:template match="@*" mode="schematron-get-full-path">
      <xsl:text>/</xsl:text>
      <xsl:choose>
         <xsl:when test="namespace-uri()=''">@<xsl:value-of select="name()"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text>@*[local-name()='</xsl:text>
            <xsl:value-of select="local-name()"/>
            <xsl:text>' and namespace-uri()='</xsl:text>
            <xsl:value-of select="namespace-uri()"/>
            <xsl:text>']</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--MODE: SCHEMATRON-FULL-PATH-2-->
   <!--This mode can be used to generate prefixed XPath for humans-->
   <xsl:template match="node() | @*" mode="schematron-get-full-path-2">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="preceding-sibling::*[name(.)=name(current())]">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>

   <!--MODE: GENERATE-ID-FROM-PATH -->
   <xsl:template match="/" mode="generate-id-from-path"/>
   <xsl:template match="text()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.text-', 1+count(preceding-sibling::text()), '-')"/>
   </xsl:template>
   <xsl:template match="comment()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.comment-', 1+count(preceding-sibling::comment()), '-')"/>
   </xsl:template>
   <xsl:template match="processing-instruction()" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.processing-instruction-', 1+count(preceding-sibling::processing-instruction()), '-')"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-from-path">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:value-of select="concat('.@', name())"/>
   </xsl:template>
   <xsl:template match="*" mode="generate-id-from-path" priority="-0.5">
      <xsl:apply-templates select="parent::*" mode="generate-id-from-path"/>
      <xsl:text>.</xsl:text>
      <xsl:value-of select="concat('.',name(),'-',1+count(preceding-sibling::*[name()=name(current())]),'-')"/>
   </xsl:template>
   <!--MODE: SCHEMATRON-FULL-PATH-3-->
   <!--This mode can be used to generate prefixed XPath for humans 
	(Top-level element has index)-->
   <xsl:template match="node() | @*" mode="schematron-get-full-path-3">
      <xsl:for-each select="ancestor-or-self::*">
         <xsl:text>/</xsl:text>
         <xsl:value-of select="name(.)"/>
         <xsl:if test="parent::*">
            <xsl:text>[</xsl:text>
            <xsl:value-of select="count(preceding-sibling::*[name(.)=name(current())])+1"/>
            <xsl:text>]</xsl:text>
         </xsl:if>
      </xsl:for-each>
      <xsl:if test="not(self::*)">
         <xsl:text/>/@<xsl:value-of select="name(.)"/>
      </xsl:if>
   </xsl:template>

   <!--MODE: GENERATE-ID-2 -->
   <xsl:template match="/" mode="generate-id-2">U</xsl:template>
   <xsl:template match="*" mode="generate-id-2" priority="2">
      <xsl:text>U</xsl:text>
      <xsl:number level="multiple" count="*"/>
   </xsl:template>
   <xsl:template match="node()" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>n</xsl:text>
      <xsl:number count="node()"/>
   </xsl:template>
   <xsl:template match="@*" mode="generate-id-2">
      <xsl:text>U.</xsl:text>
      <xsl:number level="multiple" count="*"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="string-length(local-name(.))"/>
      <xsl:text>_</xsl:text>
      <xsl:value-of select="translate(name(),':','.')"/>
   </xsl:template>
   <!--Strip characters-->
   <xsl:template match="text()" priority="-1"/>

   <!--SCHEMA METADATA-->
   <xsl:template match="/">
      <svrl:schematron-output xmlns:xs="http://www.w3.org/2001/XMLSchema"
                              xmlns:schold="http://www.ascc.net/xml/schematron"
                              xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                              title=""
                              schemaVersion="">
         <xsl:comment>
            <xsl:value-of select="$archiveDirParameter"/>   
		 <xsl:value-of select="$archiveNameParameter"/>  
		 <xsl:value-of select="$fileNameParameter"/>  
		 <xsl:value-of select="$fileDirParameter"/>
         </xsl:comment>
         <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gmd" prefix="gmd"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.isotc211.org/2005/gco" prefix="gco"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/sensorml/2.0" prefix="sml"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/1999/xlink" prefix="xlink"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/gml/3.2" prefix="gml"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.opengis.net/swe/2.0" prefix="swe"/>
         <svrl:ns-prefix-in-attribute-values uri="http://www.w3.org/2004/02/skos/core#" prefix="skos"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test remote reference without # (definition)</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M7"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test remote reference with # (defintion)</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M8"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test remote reference without # (href)</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M9"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test remote reference with # (href)</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M10"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test remote reference (responsibleParty/@uuid)</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M11"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">UUID available</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M12"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">test validTime is available and is a period</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M13"/>
         <svrl:active-pattern>
            <xsl:attribute name="name">control contact's role value</xsl:attribute>
            <xsl:apply-templates/>
         </svrl:active-pattern>
         <xsl:apply-templates select="/" mode="M14"/>
      </svrl:schematron-output>
   </xsl:template>

   <!--SCHEMATRON PATTERNS-->


   <!--PATTERN test remote reference without # (definition)-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test remote reference without # (definition)</svrl:text>

	  <!--RULE -->
   <xsl:template match="//sml:Term[@definition and not(contains(@definition, '#'))]              | //sml:Event[@definition and not(contains(@definition, '#'))]              | //swe:Quantity[@definition and not(contains(@definition, '#'))]              | //sml:ObservableProperty[@definition and not(contains(@definition, '#'))]"
                 priority="1000"
                 mode="M7">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//sml:Term[@definition and not(contains(@definition, '#'))]              | //sml:Event[@definition and not(contains(@definition, '#'))]              | //swe:Quantity[@definition and not(contains(@definition, '#'))]              | //sml:ObservableProperty[@definition and not(contains(@definition, '#'))]"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="document(@definition)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="document(@definition)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>reference does not exist <xsl:text/>
                  <xsl:value-of select="@definition"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M7"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M7"/>
   <xsl:template match="@*|node()" priority="-2" mode="M7">
      <xsl:apply-templates select="@*|*" mode="M7"/>
   </xsl:template>

   <!--PATTERN test remote reference with # (defintion)-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test remote reference with # (defintion)</svrl:text>

	  <!--RULE -->
   <xsl:template match="//sml:Term[@definition and contains(@definition, '#')]              | //sml:Event[@definition and contains(@definition, '#')]              | //swe:Quantity[@definition and contains(@definition, '#')]              | //sml:ObservableProperty[@definition and contains(@definition, '#')]"
                 priority="1000"
                 mode="M8">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//sml:Term[@definition and contains(@definition, '#')]              | //sml:Event[@definition and contains(@definition, '#')]              | //swe:Quantity[@definition and contains(@definition, '#')]              | //sml:ObservableProperty[@definition and contains(@definition, '#')]"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="document(substring-before(@definition, '#'))"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="document(substring-before(@definition, '#'))">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>reference does not exist <xsl:text/>
                  <xsl:value-of select="@definition"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M8"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M8"/>
   <xsl:template match="@*|node()" priority="-2" mode="M8">
      <xsl:apply-templates select="@*|*" mode="M8"/>
   </xsl:template>

   <!--PATTERN test remote reference without # (href)-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test remote reference without # (href)</svrl:text>

	  <!--RULE -->
   <xsl:template match="//sml:codeSpace[@xlink:href and not(contains(@xlink:href, '#'))]              | //swe:uom[@xlink:href and not(contains(@xlink:href, '#'))]              | //sml:document[@xlink:href and not(contains(@xlink:href, '#'))]"
                 priority="1000"
                 mode="M9">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//sml:codeSpace[@xlink:href and not(contains(@xlink:href, '#'))]              | //swe:uom[@xlink:href and not(contains(@xlink:href, '#'))]              | //sml:document[@xlink:href and not(contains(@xlink:href, '#'))]"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="document(@xlink:href)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="document(@xlink:href)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>reference does not exist <xsl:text/>
                  <xsl:value-of select="@xlink:href"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M9"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M9"/>
   <xsl:template match="@*|node()" priority="-2" mode="M9">
      <xsl:apply-templates select="@*|*" mode="M9"/>
   </xsl:template>

   <!--PATTERN test remote reference with # (href)-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test remote reference with # (href)</svrl:text>

	  <!--RULE -->
   <xsl:template match="//sml:codeSpace[@xlink:href and contains(@xlink:href, '#')]              | //swe:uom[@xlink:href and contains(@xlink:href, '#')]              | //sml:document[@xlink:href and contains(@xlink:href, '#')]"
                 priority="1000"
                 mode="M10">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//sml:codeSpace[@xlink:href and contains(@xlink:href, '#')]              | //swe:uom[@xlink:href and contains(@xlink:href, '#')]              | //sml:document[@xlink:href and contains(@xlink:href, '#')]"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="document(substring-before(@xlink:href,  '#'))"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="document(substring-before(@xlink:href, '#'))">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>reference does not exist <xsl:text/>
                  <xsl:value-of select="@xlink:href"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M10"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M10"/>
   <xsl:template match="@*|node()" priority="-2" mode="M10">
      <xsl:apply-templates select="@*|*" mode="M10"/>
   </xsl:template>

   <!--PATTERN test remote reference (responsibleParty/@uuid)-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test remote reference (responsibleParty/@uuid)</svrl:text>

	  <!--RULE -->
   <xsl:template match="//gmd:CI_ResponsibleParty[@uuid]" priority="1000" mode="M11">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:CI_ResponsibleParty[@uuid]"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="document(@uuid)"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="document(@uuid)">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>reference does not exist <xsl:text/>
                  <xsl:value-of select="@uuid"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M11"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M11"/>
   <xsl:template match="@*|node()" priority="-2" mode="M11">
      <xsl:apply-templates select="@*|*" mode="M11"/>
   </xsl:template>

   <!--PATTERN UUID available-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">UUID available</svrl:text>

	  <!--RULE -->
   <xsl:template match="/sml:PhysicalSystem" priority="1000" mode="M12">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="/sml:PhysicalSystem"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="string-length(./sml:identification/sml:IdentifierList/sml:identifier/sml:Term[sml:label/text()='uuid']/sml:value/text())=36"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="string-length(./sml:identification/sml:IdentifierList/sml:identifier/sml:Term[sml:label/text()='uuid']/sml:value/text())=36">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>UUID is missing or not well formatted <xsl:text/>
                  <xsl:value-of select="./sml:identification/sml:IdentifierList/sml:identifier/sml:Term[sml:label/text()='uuid']/sml:value/text()"/>
                  <xsl:text/>
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M12"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M12"/>
   <xsl:template match="@*|node()" priority="-2" mode="M12">
      <xsl:apply-templates select="@*|*" mode="M12"/>
   </xsl:template>

   <!--PATTERN test validTime is available and is a period-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">test validTime is available and is a period</svrl:text>

	  <!--RULE -->
   <xsl:template match="/sml:PhysicalSystem" priority="1000" mode="M13">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="/sml:PhysicalSystem"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="count(./sml:validTime/gml:TimePeriod/gml:beginPosition)=1 and count(./sml:validTime/gml:TimePeriod/gml:endPosition)=1"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="count(./sml:validTime/gml:TimePeriod/gml:beginPosition)=1 and count(./sml:validTime/gml:TimePeriod/gml:endPosition)=1">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>validTime is missing or incomplete.</svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M13"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M13"/>
   <xsl:template match="@*|node()" priority="-2" mode="M13">
      <xsl:apply-templates select="@*|*" mode="M13"/>
   </xsl:template>

   <!--PATTERN control contact's role value-->
   <svrl:text xmlns:xs="http://www.w3.org/2001/XMLSchema"
              xmlns:schold="http://www.ascc.net/xml/schematron"
              xmlns:svrl="http://purl.oclc.org/dsdl/svrl">control contact's role value</svrl:text>

	  <!--RULE -->
   <xsl:template match="//gmd:role/gmd:CI_RoleCode" priority="1000" mode="M14">
      <svrl:fired-rule xmlns:xs="http://www.w3.org/2001/XMLSchema"
                       xmlns:schold="http://www.ascc.net/xml/schematron"
                       xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                       context="//gmd:role/gmd:CI_RoleCode"/>
      <xsl:variable name="skosCollectionURL"
                    select="concat(substring-before(./@codeList, 'index.php?tema='),                 'xml.php?skosCollection=',                 substring-after(./@codeList, 'index.php?tema='))"/>
      <xsl:variable name="codes" select="document($skosCollectionURL)"/>

		    <!--ASSERT -->
      <xsl:choose>
         <xsl:when test="$codes//skos:Concept/skos:prefLabel[normalize-space(text()) = normalize-space(current()/@codeListValue)]"/>
         <xsl:otherwise>
            <svrl:failed-assert xmlns:xs="http://www.w3.org/2001/XMLSchema"
                                xmlns:schold="http://www.ascc.net/xml/schematron"
                                xmlns:svrl="http://purl.oclc.org/dsdl/svrl"
                                test="$codes//skos:Concept/skos:prefLabel[normalize-space(text()) = normalize-space(current()/@codeListValue)]">
               <xsl:attribute name="location">
                  <xsl:apply-templates select="." mode="schematron-get-full-path"/>
               </xsl:attribute>
               <svrl:text>
                  <xsl:text/>
                  <xsl:value-of select="./@codeListValue"/>
                  <xsl:text/> is not among prefLabel in <xsl:text/>
                  <xsl:value-of select="$skosCollectionURL"/>
                  <xsl:text/> 
               </svrl:text>
            </svrl:failed-assert>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:apply-templates select="@*|*" mode="M14"/>
   </xsl:template>
   <xsl:template match="text()" priority="-1" mode="M14"/>
   <xsl:template match="@*|node()" priority="-2" mode="M14">
      <xsl:apply-templates select="@*|*" mode="M14"/>
   </xsl:template>
</xsl:stylesheet>
