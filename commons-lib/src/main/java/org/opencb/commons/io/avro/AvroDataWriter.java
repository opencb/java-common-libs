/*
 * Copyright 2015-2016 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opencb.commons.io.avro;

import org.apache.avro.Schema;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.specific.SpecificDatumWriter;
import org.opencb.commons.ProgressLogger;
import org.opencb.commons.io.DataWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;

/**
 * Created on 09/11/15.
 *
 * @author Jacobo Coll &lt;jacobo167@gmail.com&gt;
 */
public class AvroDataWriter<T extends GenericRecord> implements DataWriter<T> {

    private Path outputPath;
    private boolean gzip;
    private DataFileWriter<T> avroWriter;
    private Schema schema;
    private ProgressLogger progressLogger;
    protected Logger logger = LoggerFactory.getLogger(this.getClass().toString());

    public AvroDataWriter(Path outputPath, boolean gzip, Schema schema) {
        this.outputPath = outputPath;
        this.gzip = gzip;
        this.schema = schema;
    }

    public AvroDataWriter setProgressLogger(ProgressLogger progressLogger) {
        this.progressLogger = progressLogger;
        return this;
    }

    @Override
    public boolean open() {

        /** Open output stream **/
        try {
            DatumWriter<T> datumWriter = new SpecificDatumWriter<>();
            avroWriter = new DataFileWriter<>(datumWriter);
            avroWriter.setCodec(gzip ? CodecFactory.deflateCodec(CodecFactory.DEFAULT_DEFLATE_LEVEL) : CodecFactory.nullCodec());
            avroWriter.create(schema, outputPath.toFile());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }


    @Override
    public boolean write(List<T> batch) {
        T last = null;
        try {
            for (T t : batch) {
                last = t;
                avroWriter.append(t);
            }
            logProgress(batch);
        } catch (IOException e) {
            logger.error("last element : " + last, e);
            throw new UncheckedIOException(e);
        } catch (RuntimeException e) {
            logger.error("last element : " + last, e);
            throw e;
        }
        return true;
    }

    protected void logProgress(List<T> batch) {
        if (progressLogger != null) {
            progressLogger.increment(batch.size());
        }
    }

    @Override
    public boolean close() {
        try {
            avroWriter.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return true;
    }
}
