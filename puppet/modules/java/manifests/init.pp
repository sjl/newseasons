class java {
  $release = regsubst(generate("/usr/bin/lsb_release", "-s", "-c"), '(\w+)\s', '\1')

  file { "partner.list":
    path => "/etc/apt/sources.list.d/partner.list",
    ensure => file,
    owner => "root",
    group => "root",
    content => "deb http://archive.canonical.com/ $release partner\ndeb-src http://archive.canonical.com/ $release partner\n",
    notify => Exec["apt-get-update"],
  }

  exec { "apt-get-update":
    command => "/usr/bin/apt-get update",
    refreshonly => true,
  }

  package { "debconf-utils":
    ensure => installed
  }

  exec { "agree-to-jre-license":
    command => "/bin/echo -e sun-java6-jre shared/accepted-sun-dlj-v1-1 select true | debconf-set-selections",
    unless => "debconf-get-selections | grep 'sun-java6-jre.*shared/accepted-sun-dlj-v1-1.*true'",
    path => ["/bin", "/usr/bin"], require => Package["debconf-utils"],
  }

  package { "sun-java6-jre":
    ensure => latest,
    require => [ File["partner.list"], Exec["agree-to-jre-license"], Exec["apt-get-update"] ],
  }
}
