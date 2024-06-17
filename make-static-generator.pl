#!/usr/bin/env perl

while (<>) {
  s/^  public(?!\s+static)/  public static/;
  s/Generator/StaticGenerator/;
  s/this::/StaticGenerator::/g;
  print;
}

__END__
