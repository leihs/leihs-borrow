import React from 'react'
import SquareImageGrid from '../components/SquareImageGrid'

const BASE_CLASS = 'ui-models-list'

export default function ModelList({ list }) {
  return <SquareImageGrid list={list} className={BASE_CLASS} itemClassName={BASE_CLASS + '-item'} />
}
