/* 
 * Copyright 2012 Phil Pratt-Szeliga and other contributors
 * http://chirrup.org/
 * 
 * See the file LICENSE for copying permission.
 */

package edu.syr.pcpratts.rootbeer.generate.opencl.tweaks;

import edu.syr.pcpratts.compressor.Compressor;
import edu.syr.pcpratts.deadmethods.DeadMethods;
import edu.syr.pcpratts.rootbeer.util.CompilerRunner;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CudaTweaks extends Tweaks {

  @Override
  public String getGlobalAddressSpaceQualifier() {
    return "";
  }

  @Override
  public String getHeaderPath() {
    return "/edu/syr/pcpratts/rootbeer/generate/opencl/CudaHeader.c";
  }
  
  @Override
  public String getGarbageCollectorPath() {
    return "/edu/syr/pcpratts/rootbeer/generate/opencl/GarbageCollector.c";
  }

  @Override
  public String getKernelPath() {
    return "/edu/syr/pcpratts/rootbeer/generate/opencl/CudaKernel.c";
  }

  public List<byte[]> compileProgram(String cuda_code) {
    try {      
      DeadMethods dead_methods = new DeadMethods("entry") ;
      cuda_code = dead_methods.filter(cuda_code);
      
      //Compressor compressor = new Compressor();
      //cuda_code = compressor.compress(cuda_code);
      
      //print out code for debugging
      File generated = new File("generated.cu");
      PrintWriter writer = new PrintWriter(generated.getAbsoluteFile());
      writer.println(cuda_code.toString());
      writer.flush();
      writer.close();

      File code_file = new File("code_file.cubin");
      //String modelString = "-m"+System.getProperty("sun.arch.data.model");
      String modelString = "";

      String command;
      if(File.separator.equals("/")){
        command = "/usr/local/cuda/bin/nvcc "+modelString+" -arch sm_20 -cubin "+generated.getAbsolutePath()+" -o "+code_file.getAbsolutePath();
      } else {
        GenerateClScript generate = new GenerateClScript();
        File cl_script = generate.execute(generated, code_file);
        command = "cmd /c \""+cl_script.getAbsolutePath()+"\"";
      }
      
      CompilerRunner runner = new CompilerRunner();
      runner.run(command);      
        
      List<byte[]> file_contents = null;
      try {
        file_contents = readFile(code_file);
      } catch(FileNotFoundException ex){
        file_contents = new ArrayList<byte[]>();
        ex.printStackTrace();
      }
      return file_contents;
    } catch(Exception ex){
      throw new RuntimeException(ex);
    }
    
  }

  private List<byte[]> readFile(File file) throws Exception {
    InputStream is = new FileInputStream(file);
    List<byte[]> ret = new ArrayList<byte[]>();
    while(true){
      byte[] buffer = new byte[4096];
      int len = is.read(buffer);
      if(len == -1)
        break;
      byte[] short_buffer = new byte[len];
      for(int i = 0; i < len; ++i){
        short_buffer[i] = buffer[i];
      }
      ret.add(short_buffer);
    }
    return ret;
  }

  @Override
  public String getDeviceFunctionQualifier() {
    return "__device__";
  }

}
