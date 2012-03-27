#!/bin/sh

#   LibreOffice extension for syntax highlighting
#   Copyright (C) 2008  Cédric Bosdonnat cedric.bosdonnat.ooo@free.fr
#
#   This library is free software; you can redistribute it and/or
#   modify it under the terms of the GNU Library General Public
#   License as published by the Free Software Foundation; 
#   version 2 of the License.
#
#   This library is distributed in the hope that it will be useful,
#   but WITHOUT ANY WARRANTY; without even the implied warranty of
#   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
#   Library General Public License for more details.
#
#   You should have received a copy of the GNU Library General Public
#   License along with this library; if not, write to the Free
#   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

OUTPUT_DIR=../config/org/libreoffice/langs
GESHI_VERSION=1.0.8

## Change this to fetch the latest language definitions from GeSHi's website
if [ ! -e geshi-$GESHI_VERSION.tar.bz2 ]
then
    wget http://surfnet.dl.sourceforge.net/sourceforge/geshi/geshi-$GESHI_VERSION.tar.bz2
    tar xjf geshi-$GESHI_VERSION.tar.bz2
fi

GESHI_DIR=geshi/geshi

mkdir -p $OUTPUT_DIR

for phpFile in `ls $GESHI_DIR/*.php`
do
    echo "Converting `basename $phpFile`"
    php ./from-geshi.php $phpFile $OUTPUT_DIR
done
