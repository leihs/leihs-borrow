class Hash
  def delete_in!(*path, key)
    dig(*path).try(:delete, key)
  end
end
