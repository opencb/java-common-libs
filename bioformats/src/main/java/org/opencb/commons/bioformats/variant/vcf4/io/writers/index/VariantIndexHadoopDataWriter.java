package org.opencb.commons.bioformats.variant.vcf4.io.writers.index;

import com.mongodb.MongoClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.opencb.commons.bioformats.variant.vcf4.VcfRecord;
import org.opencb.commons.bioformats.variant.vcf4.io.proto.VariantFieldsProtos;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: echirivella
 * Date: 10/18/13
 * Time: 2:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class VariantIndexHadoopDataWriter implements VariantIndexDataWriter {
    private String tablestr;
    private HTable table;
    private String study;
    private String hconf; //Más adelante, útil para portar.
    private HBaseAdmin admin;
    private Map<String, Put> putmap;
    private Put put2;
    private final byte[] info_cf = "i".getBytes();
    private final byte[] data_cf = "d".getBytes();
    private List<Put> put;
    private MongoClient mongocon;

    public VariantIndexHadoopDataWriter(String tabla, String studio ){
        try{
            tablestr = tabla;
            study = studio;
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.master", "172.24.79.30:60010");
            config.set("hbase.zookeeper.quorum", "172.24.79.30");
            config.set("hbase.zookeeper.property.clientPort","2181");
            admin = new HBaseAdmin(config);
            mongocon = new MongoClient("localhost");


        }catch(IOException e){
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
        VariantFieldsProtos.getDescriptor();
    }





    @Override
    public boolean writeVariantIndex(List<VcfRecord> data) {
        for(VcfRecord v: data){

            String rowkey = buildrowkey(v.getChromosome(), String.valueOf(v.getPosition()));
            VariantFieldsProtos.VariantInfo info =  infobuilder(v);
            byte[] qualdata = (study + "_data" ).getBytes();
            if(!(null==putmap.get(rowkey))){
                put2 = putmap.get(rowkey);
                put2.add(info_cf, qualdata, info.toByteArray());
                putmap.put(rowkey, put2);
            }
            else{
                put2 = new Put(rowkey.getBytes());
                put2.add(info_cf, qualdata, info.toByteArray());
                putmap.put(rowkey, put2);
            }
            for(String s: v.getSampleNames()){
                VariantFieldsProtos.VariantSample.Builder sp = VariantFieldsProtos.VariantSample.newBuilder();
                sp.setSample(v.getSampleRawData(s));
                VariantFieldsProtos.VariantSample sample = sp.build();
                byte[] qual = (study + "_" + s).getBytes();
                if(!(null==putmap.get(rowkey))){
                    put2 = putmap.get(rowkey);
                    put2.add(data_cf, qual, sample.toByteArray());
                    putmap.put(rowkey, put2);
                }
                else{
                    put2 = new Put(rowkey.getBytes());
                    put2.add(data_cf, qual, sample.toByteArray());
                    putmap.put(rowkey, put2);
                }
            }
        }
        send();
        return true;
    }


    @Override
    public boolean open() {
        try{
            Configuration config = conf();
            if(!admin.tableExists(tablestr)){
                String aux = tablestr;
                HTableDescriptor nuevatabla = new HTableDescriptor(aux.getBytes());
                HColumnDescriptor samples = new HColumnDescriptor(data_cf);
                samples.setCompressionType(Compression.Algorithm.SNAPPY);
                nuevatabla.addFamily(samples);
                HColumnDescriptor stats_info = new HColumnDescriptor(info_cf);
                stats_info.setCompressionType(Compression.Algorithm.SNAPPY);
                nuevatabla.addFamily(stats_info);
                admin.createTable(nuevatabla);
            }
            table = new HTable(config, tablestr);
            table.setAutoFlush(false, true);
            put = new ArrayList();
            putmap = new HashMap<>();
            return true;
        }catch (IOException e){
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean close() {
        try{

            admin.close();
            table.close();
            return true;
        }catch(IOException e){
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean pre() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean post() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Configuration conf(){
        Configuration config = HBaseConfiguration.create();
        config.set("hbase.master", "172.24.79.30:60010");
        config.set("hbase.zookeeper.quorum", "172.24.79.30");
        config.set("hbase.zookeeper.property.clientPort","2181");
        return config;
    }





    private void send(){
        for(String s: putmap.keySet()){
            Put put2 = putmap.get(s);
            put.add(put2);
        }
        try{
            table.put(put);
            put.clear();
            putmap.clear();
        }catch(IOException e){
            e.printStackTrace();
        }
    }






    private VariantFieldsProtos.VariantInfo infobuilder(VcfRecord v){
        String[] format = parseFormat(v.getFormat());
        String[] filter = parseFilter(v.getFilter());
        String[] infor = parseInfo(v.getInfo());
        String[] alternate = parseAlternate(v.getAlternate());
        VariantFieldsProtos.VariantInfo.Builder info = VariantFieldsProtos.VariantInfo.newBuilder();
        info.setQuality(v.getQuality());
        info.setReference(v.getReference());
        if(format != null){
            for(String s: format){
                info.addFormat(s);
            }
        }
        if(alternate != null){
            for(String s: alternate){
                info.addAlternate(s);
            }
        }
        if(filter != null){
            for(String s: filter){
                info.addFilters(s);
            }
        }
        if(infor != null){
            for(String s: infor){
                info.addInfo(s);
            }
        }
        return info.build();
    }




    private String[] parseFormat(String format){
        return format.split(":");
    }



    private String[] parseFilter(String filter){
        if(!filter.equals(".")){
            return filter.split(";");
        }
        else{
            return null;
        }
    }




    private String[] parseInfo(String info){
        if(!info.equals(".")){
            return info.split(";");
        }
        else{
            return null;
        }
    }



    private String[] parseAlternate(String alternate){
        if(!alternate.equals(".")){
            return alternate.split(",");
        }
        else{
            return null;
        }
    }




    private String buildrowkey(String chromosome, String position){
        if(chromosome.length()<2){
            chromosome = "0" + chromosome;
        }
        if(position.length()<10){
            while(position.length()<10){
                position = "0" + position;
            }
        }
        return chromosome + "_" + position;
    }


}
