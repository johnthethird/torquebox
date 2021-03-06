require 'torquebox/server'

describe TorqueBox::Server do

  it "should expand relative paths" do
    java.lang.System.setProperty('jruby.home', '.')
    TorqueBox::Server.jruby_home.should == Dir.pwd
  end

  it "should return nil if torquebox-server is not installed" do
    TorqueBox::Server.torquebox_home.should == nil
  end
  
  describe "under jruby-1.6.5" do
    it "should use torquebox-server gem's installed path" do
      server_gem = mock('server-gem')
      Gem::Specification.should_receive(:find_by_name).with('torquebox-server').and_return(server_gem)
      server_gem.stub('full_gem_path').and_return('torquebox-server-install-path')
      TorqueBox::Server.torquebox_home.should == 'torquebox-server-install-path'
    end
  end

  describe "under jruby-1.6.4" do
    it "should use Gem.searcher to find the torquebox-server install path" do
      # make rubygems lie to us
      old_gems = Gem::Version.new( '1.5.1' )
      TorqueBox::Server.stub!( :gem_version ).and_return( old_gems )
      
      # mocks for rubygems
      server_gem = mock('server-gem')
      searcher   = mock('searcher')
      Gem.stub!(:searcher).and_return(searcher)
      searcher.stub!('find').and_return(server_gem)
      server_gem.stub('full_gem_path').and_return('torquebox-server-install-path')

      TorqueBox::Server.torquebox_home.should == 'torquebox-server-install-path'
    end
  end
end
