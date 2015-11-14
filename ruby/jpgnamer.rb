require 'rubygems'
require 'exifr'
require 'find'

OUTPUT_PATH="output"
PREFIX="D7200_"

def getTargetPath(file)
    obj=EXIFR::JPEG.new(file)
    if obj.exif?
        hash= obj.exif.to_hash
        hash.each_pair do |k, v|
            next if k.to_s != "date_time"
            arr = v.to_s.split(" ")
            xdate = arr[0]
            xtime = arr[1].gsub(":", "") 
            targetPath="#{OUTPUT_PATH}/#{xdate}/#{PREFIX}#{xdate}_#{xtime}.jpg"
            #puts targetPath
            return checkExist(file, targetPath)

            #return v.to_s.split(" ")[0] if k.to_s == "date_time"
        end
    end
    return nil
end

def checkExist(src, tag)
   if(File.exist?(tag))
        puts "File exist:#{tag}" 
        if(File.size(src) == File.size(tag) )
            puts "Same file exist:#{tag}, skip it."
	    return nil
        else
           (1..10).each { |xi|
               tempTag = tag.gsub(".jpg", "_#{xi}.jpg")
               if(! File.exist?(tempTag))
                    return tempTag
               end 
           } 
        end
    else 
       puts "Not exist:#{tag}"
       return tag
    end 
end


DIR=ARGV[0]
puts DIR
`mkdir #{OUTPUT_PATH}`
    
if File.directory?(DIR)
    Dir.foreach(DIR) do |filename|
    if filename != "." and filename != ".."
        filePath="#{DIR}/#{filename}"
        puts "----> #{filePath}"
        begin 
            targetPath=getTargetPath(filePath)
        rescue
            next
        end

        if targetPath.nil?
            puts "Get target file path failed:#{filename}"
            next
        end
        p = targetPath.split('/')[1]
        `mkdir -p #{OUTPUT_PATH}/#{p}`
        puts "Move #{filePath} -> #{targetPath}"
        #`mv #{filePath} #{targetPath}`
        `cp #{filePath} #{targetPath}`
        
    end
    end 
end
#print getTargetPath(ARGV[0])

