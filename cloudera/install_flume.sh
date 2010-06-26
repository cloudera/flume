#!/bin/sh
# Copyright 2009 Cloudera, inc.
set -ex

usage() {
  echo "
usage: $0 <options>
  Required not-so-options:
     --cloudera-source-dir=DIR   path to cloudera distribution files
     --build-dir=DIR             path to flumedist.dir
     --prefix=PREFIX             path to install into

  Optional options:
     --doc-dir=DIR               path to install docs into [/usr/share/doc/flume]
     --lib-dir=DIR               path to install flume home [/usr/lib/flume]
     --installed-lib-dir=DIR     path where lib-dir will end up on target system
     --bin-dir=DIR               path to install bins [/usr/bin]
     --examples-dir=DIR          path to install examples [doc-dir/examples]
     ... [ see source for more similar options ]
  "
  exit 1
}

OPTS=$(getopt \
  -n $0 \
  -o '' \
  -l 'cloudera-source-dir:' \
  -l 'prefix:' \
  -l 'doc-dir:' \
  -l 'lib-dir:' \
  -l 'installed-lib-dir:' \
  -l 'bin-dir:' \
  -l 'examples-dir:' \
  -l 'build-dir:' -- "$@")

if [ $? != 0 ] ; then
    usage
fi

eval set -- "$OPTS"
set -ex
while true ; do
    case "$1" in
        --cloudera-source-dir)
        CLOUDERA_SOURCE_DIR=$2 ; shift 2
        ;;
        --prefix)
        PREFIX=$2 ; shift 2
        ;;
        --build-dir)
        BUILD_DIR=$2 ; shift 2
        ;;
        --doc-dir)
        DOC_DIR=$2 ; shift 2
        ;;
        --lib-dir)
        LIB_DIR=$2 ; shift 2
        ;;
        --installed-lib-dir)
        INSTALLED_LIB_DIR=$2 ; shift 2
        ;;
        --bin-dir)
        BIN_DIR=$2 ; shift 2
        ;;
        --examples-dir)
        EXAMPLES_DIR=$2 ; shift 2
        ;;
        --)
        shift ; break
        ;;
        *)
        echo "Unknown option: $1"
        usage
        exit 1
        ;;
    esac
done

for var in CLOUDERA_SOURCE_DIR PREFIX BUILD_DIR ; do
  if [ -z "$(eval "echo \$$var")" ]; then
    echo Missing param: $var
    usage
  fi
done

MAN_DIR=${MAN_DIR:-/usr/share/man/man1}
DOC_DIR=${DOC_DIR:-/usr/share/doc/flume}
LIB_DIR=${LIB_DIR:-/usr/lib/flume}
BIN_DIR=${BIN_DIR:-/usr/lib/flume/bin}
CONF_DIR=/etc/flume/
CONF_DIST_DIR=/etc/flume/conf.dist/
ETC_DIR=${ETC_DIR:-/etc/flume}


install -d -m 0755 ${PREFIX}/${LIB_DIR}

install -d -m 0755 ${PREFIX}/${LIB_DIR}/lib
for i in `find lib/*.jar build/lib/*.jar -type f |grep -v zookeeper`
	do echo "Copying $i"
	cp $i ${PREFIX}/${LIB_DIR}/lib #don't copy directories by default
done

cp flume*.jar ${PREFIX}/${LIB_DIR}/lib

cp -a webapps ${PREFIX}/${LIB_DIR}

install -d -m 0755 $PREFIX/$BIN_DIR
cp bin/* $PREFIX/${BIN_DIR}

wrapper=$PREFIX/usr/bin/flume
mkdir -p `dirname $wrapper`
cat > $wrapper <<EOF
#!/bin/sh

exec /usr/lib/flume/bin/flume "\$@"
EOF
chmod 755 $wrapper


install -d -m 0755 $PREFIX/$ETC_DIR/conf.empty
(cd ${BUILD_DIR}/conf && tar cf - .) | (cd $PREFIX/$ETC_DIR/conf.empty && tar xf -)

unlink $PREFIX/$LIB_DIR/conf || /bin/true
ln -s /etc/flume/conf $PREFIX/$LIB_DIR/conf

# Docs
install -d -m 0755 $PREFIX/${DOC_DIR}
cp -r docs/* $PREFIX/${DOC_DIR}


# man pages
install -d -m 0755 $PREFIX/$MAN_DIR
gzip -c $CLOUDERA_SOURCE_DIR/../docs/man/flume.1 > $PREFIX/$MAN_DIR/flume.1.gz
gzip -c $CLOUDERA_SOURCE_DIR/../docs/man/flume-master.1 > $PREFIX/$MAN_DIR/flume-master.1.gz
gzip -c $CLOUDERA_SOURCE_DIR/../docs/man/flume-node.1 > $PREFIX/$MAN_DIR/flume-node.1.gz
gzip -c $CLOUDERA_SOURCE_DIR/../docs/man/flume-shell.1 > $PREFIX/$MAN_DIR/flume-shell.1.gz
