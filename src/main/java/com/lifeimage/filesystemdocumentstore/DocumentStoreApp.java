package com.lifeimage.filesystemdocumentstore;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.TimeUnit;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.google.common.base.Stopwatch;
import com.lifeimage.documentstore.service.DocumentStore;

/**
 * Hello world!
 *
 */
public class DocumentStoreApp
{

    private static final Logger log = LoggerFactory.getLogger(DocumentStoreApp.class.getName());

    public static void main(final String[] args) throws Exception
    {

        @SuppressWarnings("resource")
        final ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        final DocumentStore documentStore = (DocumentStore) ctx.getBean("documentstore");

        final Options options = new Options();

        // add  options
        final OptionGroup optgrp = new OptionGroup();
        optgrp.setRequired(true);
        optgrp.addOption(new Option("u", false, "upload"));
        optgrp.addOption(new Option("d", false, "download"));

        options.addOptionGroup(optgrp);
        options.addOption("i", true, "document id");
        options.addOption("f", true, "file path");

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd = parser.parse(options, args);
        final Stopwatch stopwatch = Stopwatch.createUnstarted();

        if (cmd.hasOption("u"))
        {
            final String filePath = cmd.getOptionValue("f");

            try (final InputStream inputStream = new FileInputStream(filePath))
            {
                stopwatch.elapsed(TimeUnit.SECONDS);
                stopwatch.start();

                final String documentId = documentStore.create(inputStream);

                log.info("document store upload time [{}]  document id  [{}]", stopwatch.toString(), documentId);
            }

            catch (final Exception e)
            {
                log.error("error uploading file to document store upload time {} ", stopwatch.toString(), e);

            }
        }
        if (cmd.hasOption("d"))
        {
            try
            {
                final String filePath = cmd.getOptionValue("f");

                final String documentId = cmd.getOptionValue("i");

                stopwatch.elapsed(TimeUnit.SECONDS);
                stopwatch.start();

                final InputStream contentStream = documentStore.get(documentId);
                Files.copy(contentStream, Paths.get(filePath), StandardCopyOption.REPLACE_EXISTING);

                log.info("document store file download time " + stopwatch.toString());
            }

            catch (final Exception e)
            {
                log.error("error downloading file from document store {} ", stopwatch.toString(), e);

            }
        }
    }
}
