package org.opencb.commons.bioformats.variant.vcf4.io.writers.stats;

import com.mongodb.MongoClient;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.io.hfile.Compression;
import org.apache.hadoop.hbase.util.Bytes;
import org.opencb.commons.bioformats.variant.vcf4.io.proto.VariantFieldsProtos;
import org.opencb.commons.bioformats.variant.vcf4.stats.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VariantStatsHadoopDataWriter implements VariantStatsDataWriter {
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



    public VariantStatsHadoopDataWriter(String tabla, String studio ){
        try{
            mongocon = new MongoClient("localhost");
            tablestr = tabla;
            study = studio;
            Configuration config = HBaseConfiguration.create();
            config.set("hbase.master", "172.24.79.30:60010");
            config.set("hbase.zookeeper.quorum", "172.24.79.30");
            config.set("hbase.zookeeper.property.clientPort","2181");
            admin = new HBaseAdmin(config);
        }catch(IOException e){
            e.printStackTrace();
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }

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
        return true;
    }

    @Override
    public boolean post() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    public boolean writeVariantStats(List<VcfVariantStat> data) {
        for(VcfVariantStat v: data ){
            String rowkey = buildrowkey(v.getChromosome(), String.valueOf(v.getPosition()));
            VariantFieldsProtos.VariantStats stats = statsbuilder(v);
            byte[] qual = (study + "_stats").getBytes();
            put2 = new Put(Bytes.toBytes(rowkey));
            put2.add(info_cf, qual, stats.toByteArray());
            putmap.put(rowkey, put2);
        }
        send();
        return true;
    }



    @Override
    public boolean writeGlobalStats(VcfGlobalStat globalStats) {

        //Mongo
        return false;
    }

    @Override
    public boolean writeSampleStats(VcfSampleStat vcfSampleStat) {
        //TODO
        return false;
    }

    @Override
    public boolean writeSampleGroupStats(VcfSampleGroupStat vcfSampleGroupStat)
            throws IOException {
        // TODO Mongo
        return false;
    }

    @Override
    public boolean writeVariantGroupStats(VcfVariantGroupStat groupStats)
            throws IOException {
        // TODO Auto-generated method stub
        return false;
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





    private VariantFieldsProtos.VariantStats statsbuilder(VcfVariantStat v){
        VariantFieldsProtos.VariantStats.Builder stats = VariantFieldsProtos.VariantStats.newBuilder();
        stats.setNumAlleles(v.getNumAlleles());
        stats.setMafAllele(v.getMafAllele());
        stats.setMgfGenotype(v.getMgfAllele());
        stats.setMaf(v.getMaf());
        stats.setMgf(v.getMgf());
        for(int a: v.getAllelesCount()){
            stats.addAllelesCount(a);
        }
        for(int a: v.getGenotypesCount()){
            stats.addGenotypesCount(a);
        }
        for(float a: v.getAllelesFreq()){
            stats.addAllelesFreq(a);
        }
        for(float a: v.getGenotypesFreq()){
            stats.addGenotypesFreq(a);
        }
        stats.setMissingAlleles(v.getMissingAlleles());
        stats.setMissingGenotypes(v.getMissingGenotypes());
        stats.setMendelianErrors(v.getMendelinanErrors());
        stats.setIsIndel(v.getIndel());
        stats.setCasesPercentDominant(v.getCasesPercentDominant());
        stats.setControlsPercentDominant(v.getControlsPercentDominant());
        stats.setCasesPercentRecessive(v.getCasesPercentRecessive());
        stats.setControlsPercentRecessive(v.getControlsPercentRecessive());
        //stats.setHardyWeinberg(v.getHw().getpValue());
        return stats.build();
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
