require "net/scp"

module Yt
  def self.move_file(from, to, host)
    username = host.netssh_options[:user]
    password = host.netssh_options[:password]
    puts "#{username}@#{host.hostname}:#{to}"
    puts ""
	last = 0
    ::Net::SCP.upload!(
        String(host.hostname),
        username,
        from,
        to,
        :ssh => host.netssh_options
    ) do |ch, name, sent, total|
      print "copying #{name} : #{sent} / #{total} = #{sent*100/total}%\n"
    end
    puts ""
  end
end
