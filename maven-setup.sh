#!/bin/bash

function maven_install()
{
	MAVEN_LIBRARY="${1}"
	FILE_TOKENS=(${MAVEN_LIBRARY//\// })

	GROUP_ID="${FILE_TOKENS[1]}"
	ARTIFACT_ID="${FILE_TOKENS[2]}"
	VERSION="${FILE_TOKENS[3]}"

	echo "Installing ${MAVEN_LIBRARY} as ${GROUP_ID}:${ARTIFACT_ID}:${VERSION}"

	mvn install:install-file -DgroupId=${GROUP_ID} -DartifactId=${ARTIFACT_ID} -Dversion=${VERSION} -Dpackaging=jar -Dfile=${MAVEN_LIBRARY} -DgeneratePom=true

	echo
}

function process_lib_dir()
{
	LIB_DIR="${1}"

	cd "${LIB_DIR}"
	for F in $(find . -type f -iname *.jar -print)
	do
		maven_install ${F}
	done

	cd -
}



for D in $(find . -type d -name maven-static-lib -print)
do
	process_lib_dir ${D}
done
