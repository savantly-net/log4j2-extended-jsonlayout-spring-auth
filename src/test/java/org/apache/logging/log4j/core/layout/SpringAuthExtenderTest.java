/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */
package org.apache.logging.log4j.core.layout;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.categories.Layouts;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.BasicConfigurationFactory;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.test.appender.ListAppender;
import org.apache.logging.log4j.util.Strings;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import net.savantly.log4j.layout.SpringAuthExtender;

/**
 * Tests the ExtendedJsonLayout class.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
@Category(Layouts.Json.class)
public class SpringAuthExtenderTest {
    static ConfigurationFactory cf = new BasicConfigurationFactory();

    @AfterClass
    public static void cleanupClass() {
        ConfigurationFactory.removeConfigurationFactory(cf);
        ThreadContext.clearAll();
    }

    @BeforeClass
    public static void setupClass() {
        ThreadContext.clearAll();
        ConfigurationFactory.setConfigurationFactory(cf);
        final LoggerContext ctx = LoggerContext.getContext();
        ctx.reconfigure();
    }

    LoggerContext ctx = LoggerContext.getContext();

    Logger rootLogger = this.ctx.getRootLogger();

    private void checkAt(final String expected, final int lineIndex, final List<String> list) {
        final String trimedLine = list.get(lineIndex).trim();
        assertTrue("Incorrect line index " + lineIndex + ": " + Strings.dquote(trimedLine), trimedLine.equals(expected));
    }

    private void checkContains(final String expected, final List<String> list) {
        for (final String string : list) {
            final String trimedLine = string.trim();
            if (trimedLine.equals(expected)) {
                return;
            }
        }
        Assert.fail("Cannot find " + expected + " in " + list);
    }

    @Test
    @WithMockUser(username="testUser")
    public void testMixedInFields() throws Exception {
        final Map<String, Appender> appenders = this.rootLogger.getAppenders();
        for (final Appender appender : appenders.values()) {
            this.rootLogger.removeAppender(appender);
        }
        final Configuration configuration = rootLogger.getContext().getConfiguration();
        // set up appender
        final boolean propertiesAsList = false;
        // @formatter:off
        final AbstractJacksonLayout layout = ExtendedJsonLayout.newBuilder()
                .setConfiguration(configuration)
                .setLocationInfo(true)
                .setProperties(true)
                .setPropertiesAsList(propertiesAsList)
                .setComplete(true)
                .setCompact(false)
                .setEventEol(false)
                .setIncludeStacktrace(true)
                .setJsonAdapter(SpringAuthExtender.class.getName())
                .build();
        // @formatter:on
        final ListAppender appender = new ListAppender("List", null, layout, true, false);
        appender.start();

        // set appender on root and set level to debug
        this.rootLogger.addAppender(appender);
        this.rootLogger.setLevel(Level.DEBUG);

        // output starting message
        this.rootLogger.debug("should have the mixedIn field containing a 'hostname' value");

        appender.stop();
        

        final List<String> list = appender.getMessages();
        
        // Send the messages to the console
        for (int msgIndex = 0; msgIndex < list.size(); msgIndex++) {
        	System.out.println(list.get(msgIndex));
		}
        

        this.checkAt("[", 0, list);
        this.checkAt("{", 1, list);
        this.checkContains("\"level\" : \"DEBUG\",", list);
        this.checkContains("\"loggerFqcn\" : \"" + AbstractLogger.class.getName() + "\",", list);
        this.checkContains("\"hostname\" : \""+ InetAddress.getLocalHost().getHostName() +"\",", list);
        this.checkContains("\"authentication\" : {", list);
        this.checkContains("\"username\" : \"testUser\"", list);
        for (final Appender app : appenders.values()) {
            this.rootLogger.addAppender(app);
        }
    }
    
    @org.springframework.context.annotation.Configuration
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    static class Config {
        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
            auth
                .inMemoryAuthentication()
                    .withUser("user").password("password").roles("USER");
        }
    }

}